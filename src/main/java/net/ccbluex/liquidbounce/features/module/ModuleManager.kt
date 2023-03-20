/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.special.AutoDisable
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard

class ModuleManager : Listenable {

    val modules = mutableListOf<Module>()
    private val moduleClassMap = hashMapOf<Class<*>, Module>()
    fun getModuleInCategory(category: ModuleCategory) = modules.filter { it.category == category }
    var pendingBindModule: Module? = null

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.logInfo("[ModuleManager] Loading modules...")

        ClassUtils.resolvePackage("${this.javaClass.`package`.name}.modules", Module::class.java)
            .forEach(this::registerModule)

        modules.forEach { it.onInitialize() }

        modules.forEach { it.onLoad() }

        LiquidBounce.eventManager.registerListener(AutoDisable)

        ClientUtils.logInfo("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module
        modules.sortBy { it.name }

        generateCommand(module)

        LiquidBounce.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: IllegalAccessException) {
            // this module is a kotlin object
            registerModule(ClassUtils.getObjectInstance(moduleClass) as Module)
        } catch (e: Throwable) {
            ClientUtils.logError("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        LiquidBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        if (!module.moduleCommand) {
            return
        }

        val values = module.values

        if (values.isEmpty()) {
            return
        }

        LiquidBounce.commandManager.registerCommand(ModuleCommand(module, values))
    }

    fun getModulesByName(name: String): List<Module> {
        return this.modules.filter { it.name.lowercase().contains(name.lowercase()) }
    }

    /**
     * Get module by [moduleClass]
     */
    fun <T : Module> getModule(moduleClass: Class<T>): T? {
        return moduleClassMap[moduleClass] as T?
    }

    operator fun <T : Module> get(clazz: Class<T>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    fun getKeyBind(key: Int) = modules.filter { it.keyBind == key }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    private var skip = 0
    @EventTarget
    private fun onKey(event: KeyEvent) {
        if (pendingBindModule == null) {
            modules.toMutableList().filter { it.triggerType == EnumTriggerType.TOGGLE && it.keyBind == event.key }.forEach { it.toggle() }
        } else {
            skip++
            if (skip <= 1) {
                return
            }
            skip = 0
            pendingBindModule!!.keyBind = event.key
            ClientUtils.displayAlert("Bound module §a§l${pendingBindModule!!.name}§3 to key §a§l${Keyboard.getKeyName(event.key)}§3.")
            LiquidBounce.hud.addNotification(Notification("KeyBind", "Bound ${pendingBindModule!!.name} to ${Keyboard.getKeyName(event.key)}.", NotifyType.INFO))
            pendingBindModule = null
        }
    }

    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        if (pendingBindModule != null || Minecraft.getMinecraft().currentScreen != null) {
            return
        }
        modules.filter { it.triggerType == EnumTriggerType.PRESS }.forEach { it.state = Keyboard.isKeyDown(it.keyBind) }
    }

    override fun handleEvents() = true
}