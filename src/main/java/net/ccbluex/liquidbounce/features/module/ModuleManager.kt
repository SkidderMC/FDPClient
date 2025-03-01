/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommand
import net.ccbluex.liquidbounce.utils.client.ClassUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
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
    operator fun get(moduleName: String) = MODULE_REGISTRY.find { it.name.equals(moduleName, ignoreCase = true) }
    @Deprecated(message = "Only for outdated scripts", replaceWith = ReplaceWith("get(moduleClass)"))
    fun getModule(moduleClass: Class<out Module>) = get(moduleClass)

    /**
     * Get modules by [category]
     */
    operator fun get(category: Category) = MODULE_REGISTRY.filter { it.category === category }

    @Deprecated(message = "Only for outdated scripts", replaceWith = ReplaceWith("get(moduleName)"))
    fun getModule(moduleName: String) = get(moduleName)

    fun getKeyBind(key: Int) = MODULE_REGISTRY.filter { it.keyBind == key }

    /**
     * Handle incoming key presses
     */
    private val onKey = handler<KeyEvent> { event ->
        MODULE_REGISTRY.forEach { if (it.keyBind == event.key) it.toggle() }
    }

}