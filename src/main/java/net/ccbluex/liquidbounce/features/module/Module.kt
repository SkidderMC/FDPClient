/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.file.FileManager.modulesConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.handler.lang.translation
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clearTicked
import org.lwjgl.input.Keyboard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    legacyNames: Array<String> = emptyArray(),
) : Configurable(name), MinecraftInstance, Listenable {

    private val enabledEffects = mutableListOf<Pair<CoroutineDispatcher, suspend CoroutineScope.() -> Unit>>()
    private val enabledEffectJobs = mutableListOf<Job>()

    init {
        aliases(*legacyNames)
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
    fun getName(spaced: Boolean = false) = if (spaced) spacedName else name

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
            notifyBindChanged()
        }

    var bindAction = ModuleBindAction.TOGGLE
        set(action) {
            field = action

            saveConfig(modulesConfig)
            notifyBindChanged()
        }

    var bindModifiers: Set<ModuleBindModifier> = emptySet()
        set(modifiers) {
            field = modifiers

            saveConfig(modulesConfig)
            notifyBindChanged()
        }

    // Tell the NextGen web ClickGUI a bind changed so its module list/cards refresh. The bind is a
    // plain field (not a Value), so it skips Value's change notification; without this, binds set in
    // game (e.g. via the .bind command) show as "None" until the menu is fully reloaded. Suppressed
    // during bulk config loading, matching Value.changeValue.
    private fun notifyBindChanged() {
        if (!ConfigSystem.isLoadingConfig) {
            ClientChangeBus.publish(ClientChange.ValueState(name, "Bind"))
        }
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
            ModuleFeedback.settingsReset(this@Module.name)
            saveConfig(valuesConfig)
        }
        return@onChange false
    }

    override val description
        get() = forcedDescription ?: translation("module.${name.toLowerCamelCase()}.description")

    var slideStep = 0F

    // Current state of module
    var state = defaultState
        set(value) {
            if (field == value)
                return

            // Gated-off "enable" (canBeEnabled = false, e.g. ClickGUI/HudDesigner one-shot modules):
            // run the one-shot action only. No toggle/sound/notification and the field stays false, so
            // it never looks or sounds enabled when it actually isn't.
            if (value && !canBeEnabled) {
                onEnable()
                return
            }

            val previousState = field

            // Call toggle
            onToggle(value)

            // Clear ticked actions
            clearTicked()

            // Call on enabled or disabled. Set the backing field BEFORE onEnable() so coroutines/workers
            // started in onEnable that read `state` see the enabled value (avoids the start-then-exit race).
            if (value) {
                field = true
                try {
                    onEnable()
                    if (field) startEnabledEffects()
                } catch (throwable: Throwable) {
                    field = false
                    enabledEffectJobs.forEach(Job::cancel)
                    enabledEffectJobs.clear()
                    throw throwable
                }
            } else {
                enabledEffectJobs.forEach(Job::cancel)
                enabledEffectJobs.clear()
                onDisable()
                RotationUtils.cancelTargetRotation(this)
                field = false
            }

            ModuleFeedback.toggled(name, field)

            // Save module state
            saveConfig(modulesConfig)

            if (field != previousState && !ConfigSystem.isLoadingConfig) {
                ClientChangeBus.publish(ClientChange.ModuleState(name, field, isHidden))
            }
        }


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

    /** Registers a coroutine that starts on every enable and is cancelled on disable. */
    protected fun enabledEffect(
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        effect: suspend CoroutineScope.() -> Unit,
    ) {
        enabledEffects += dispatcher to effect
    }

    private fun startEnabledEffects() {
        enabledEffectJobs.forEach(Job::cancel)
        enabledEffectJobs.clear()
        enabledEffects.mapTo(enabledEffectJobs) { (dispatcher, effect) ->
            EventManager.launch(dispatcher, block = effect)
        }
    }

    /**
     * Get value by [valueName]
     */
    fun getValue(valueName: String) = findDeep(valueName)

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
