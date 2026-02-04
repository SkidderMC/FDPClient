/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.FDPClient.isStarting
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.file.FileManager.modulesConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.handler.lang.translation
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Arraylist
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.playSound
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clearTicked
import org.lwjgl.input.Keyboard

private val SPLIT_REGEX = "(?<=[a-z])(?=[A-Z])".toRegex()

open class Module(

    name: String,
    val category: Category,
    val subCategory: Category.SubCategory = Category.SubCategory.GENERAL,
    defaultKeyBind: Int = Keyboard.KEY_NONE,
    private val canBeEnabled: Boolean = true,
    private val forcedDescription: String? = null,
    var expanded: Boolean = false,

    // Adds spaces between lowercase and uppercase letters (KillAura -> Kill Aura)
    val spacedName: String = name.splitToSequence(SPLIT_REGEX).joinToString(separator = " "),
    subjective: Boolean = category == Category.VISUAL,
    val gameDetecting: Boolean = canBeEnabled,
    defaultState: Boolean = false,
    defaultHidden: Boolean = false,
) : Configurable(name), MinecraftInstance, Listenable {

    init {
        if (subjective) {
            subjective()
        }
    }

    // Value that determines whether the module should depend on GameDetector
    private val onlyInGameValue = boolean("OnlyInGame", true) {
        GameDetector.state
    }.subjective().excludeWhen(!gameDetecting)

    // Module information

    // Get normal or spaced name
    fun getName(spaced: Boolean = Arraylist.spacedModulesValue.get()) = if (spaced) spacedName else name

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
        }

    var isHidden: Boolean by boolean("Hide", defaultHidden).subjective().onChanged {
        saveConfig(modulesConfig)
    }

    private val resetValue = boolean("Reset", false).subjective().onChange { _, _ ->
        try {
            values.forEach { if (it !== this) it.resetValue() else return@forEach }
        } catch (any: Exception) {
            LOGGER.error("Failed to reset all values", any)
            chat("Failed to reset all values: ${any.message}")
        } finally {
            addNotification(Notification("Successfully reset all settings from ${this@Module.name}", "Successfully reset all settings from ${this@Module.name}", Type.SUCCESS, 1000))
            saveConfig(valuesConfig)
        }
        return@onChange false
    }

    val description
        get() = forcedDescription ?: translation("module.${name.toLowerCamelCase()}.description")

    var slideStep = 0F

    // Current state of module
    var state = defaultState
        set(value) {
            if (field == value)
                return

            // Call toggle
            onToggle(value)

            // Clear ticked actions
            clearTicked()

            // Play sound and add notification
            if (!isStarting) {
                mc.playSound("random.click".asResourceLocation())

                addNotification(Notification(name,"${if (value) "Enabled" else "Disabled"} §r$name", if (value) Type.SUCCESS else Type.ERROR, 1000))
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canBeEnabled)
                    field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            saveConfig(modulesConfig)
        }


    // HUD
    val hue = nextFloat()
    var slide = 0F
    var yAnim = 0f

    // Tag
    open val tag: String?
        get() = null

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Called when module initialized
     */
    open fun onInitialize() {}

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Called when module unregistered (for scripts)
     */
    open fun onUnregister() {}

    /**
     * Get value by [valueName]
     */
    fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get value via `module[valueName]`
     */
    operator fun get(valueName: String) = getValue(valueName)

    val isActive
        get() = !gameDetecting || !onlyInGameValue.get() || GameDetector.isInGame()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state && isActive
}