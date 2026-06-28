/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.KeyStateEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommand
import net.ccbluex.liquidbounce.utils.client.ClassUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import org.lwjgl.input.Keyboard
import java.util.*

val MODULE_REGISTRY = TreeSet(Comparator.comparing(Module::name))

object ModuleManager : Listenable, Collection<Module> by MODULE_REGISTRY {

    fun getModuleInCategory(category: Category) = MODULE_REGISTRY.filter { it.category == category }

    /**
     * Register all modules
     */
    fun registerModules() {
        LOGGER.info("[ModuleManager] Loading modules...")

        // Register modules
        ClassUtils.resolvePackage("${this.javaClass.`package`.name}.modules", Module::class.java)
            .forEach { moduleClass ->
                try {
                    registerModule(moduleClass.newInstance())
                } catch (e: IllegalAccessException) {
                    // Handle Kotlin object modules
                    val instance = ClassUtils.getObjectInstance(moduleClass) as? Module
                    if (instance != null) {
                        registerModule(instance)
                    } else {
                        LOGGER.error("Failed to instantiate module: ${moduleClass.name}")
                    }
                } catch (e: Throwable) {
                    LOGGER.error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
                }
            }

        MODULE_REGISTRY.forEach {
            it.onInitialize()
            // SplashProgress.setSecondary("Initializing Module " + it.name)
        }

        LOGGER.info("[ModuleManager] Loaded ${MODULE_REGISTRY.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        MODULE_REGISTRY += module
        generateCommand(module)
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Module) = modules.forEach(this::registerModule)

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        MODULE_REGISTRY.remove(module)
        module.onUnregister()
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        registerCommand(ModuleCommand(module, values))
    }

    /**
     * Get module by [moduleClass]
     */
    operator fun get(moduleClass: Class<out Module>) = MODULE_REGISTRY.find { it.javaClass === moduleClass }

    /**
     * Get module by [moduleName]
     */
    operator fun get(moduleName: String) = MODULE_REGISTRY.find { it.matchesKey(moduleName) }
    @Deprecated(message = "Only for outdated scripts", replaceWith = ReplaceWith("get(moduleClass)"))
    fun getModule(moduleClass: Class<out Module>) = get(moduleClass)

    /**
     * Get modules by [category]
     */
    operator fun get(category: Category) = MODULE_REGISTRY.filter { it.category === category }

    @Deprecated(message = "Only for outdated scripts", replaceWith = ReplaceWith("get(moduleName)"))
    fun getModule(moduleName: String) = get(moduleName)

    fun getKeyBind(key: Int) = MODULE_REGISTRY.filter { it.keyBind == key }

    private val smartPressedStates = mutableMapOf<Module, Pair<Boolean, Long>>()

    /**
     * Handle incoming key presses and releases.
     */
    private val onKeyState = handler<KeyStateEvent> { event ->
        if (event.key == Keyboard.KEY_NONE) {
            return@handler
        }

        MODULE_REGISTRY.forEach { module ->
            if (module.keyBind == Keyboard.KEY_NONE) {
                return@forEach
            }

            val keyMatches = module.keyBind == event.key
            val releasedModifier = !event.pressed && module.bindModifiers.any { it.matchesKey(event.key) }

            when (module.bindAction) {
                ModuleBindAction.TOGGLE -> {
                    if (event.pressed && keyMatches && module.bindModifiersPressed()) {
                        module.toggle()
                    }
                }

                ModuleBindAction.HOLD -> {
                    when {
                        event.pressed && keyMatches && module.bindModifiersPressed() -> module.state = true
                        !event.pressed && (keyMatches || releasedModifier) -> module.state = false
                    }
                }

                ModuleBindAction.SMART -> {
                    when {
                        event.pressed && keyMatches && module.bindModifiersPressed() -> {
                            smartPressedStates[module] = module.state to System.currentTimeMillis()
                            module.state = true
                        }
                        !event.pressed && keyMatches -> {
                            val (wasEnabled, pressedAt) = smartPressedStates.remove(module) ?: return@forEach
                            val held = System.currentTimeMillis() - pressedAt >= SMART_HOLD_THRESHOLD_MS
                            module.state = if (held) false else !wasEnabled
                        }
                        releasedModifier && smartPressedStates.remove(module) != null -> module.state = false
                    }
                }
            }
        }
    }

    /**
     * Safety net for HOLD/SMART binds. Key-release events are only delivered while no screen is open
     * (the release fires from the mixin only when currentScreen == null), so holding a bind key and
     * then opening a GUI - or losing window focus - would otherwise strand the module enabled because
     * its release is never seen. Every tick we reconcile against the real keyboard state and let go of
     * the module once the keys are physically up.
     */
    private val onBindTick = handler<GameTickEvent> {
        MODULE_REGISTRY.forEach { module ->
            if (module.keyBind == Keyboard.KEY_NONE) {
                return@forEach
            }
            when (module.bindAction) {
                ModuleBindAction.HOLD -> {
                    if (module.state && !module.bindHeldPhysically()) {
                        module.state = false
                    }
                }

                ModuleBindAction.SMART -> {
                    val (wasEnabled, pressedAt) = smartPressedStates[module] ?: return@forEach
                    when {
                        !Keyboard.isKeyDown(module.keyBind) -> {
                            smartPressedStates.remove(module)
                            val held = System.currentTimeMillis() - pressedAt >= SMART_HOLD_THRESHOLD_MS
                            module.state = if (held) false else !wasEnabled
                        }
                        !module.bindModifiersPressed() -> {
                            smartPressedStates.remove(module)
                            module.state = false
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun Module.bindModifiersPressed() = bindModifiers.all { it.isPressed() }

    private fun Module.bindHeldPhysically() =
        keyBind != Keyboard.KEY_NONE && Keyboard.isKeyDown(keyBind) && bindModifiersPressed()

    private const val SMART_HOLD_THRESHOLD_MS = 250L

}
