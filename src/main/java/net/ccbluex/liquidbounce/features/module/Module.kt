/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.module.modules.client.Modules
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.value.Value
import org.lwjgl.input.Keyboard

open class Module : MinecraftInstance(), Listenable {
    // Module information
    var name: String
    var localizedName = ""
        get() = field.ifEmpty { name }
    var description: String
    var category: ModuleCategory
    var keyBind = Keyboard.CHAR_NONE
        set(keyBind) {
            field = keyBind

            if (!LiquidBounce.isStarting) {
                LiquidBounce.configManager.smartSave()
            }
        }
    var array = true
        set(array) {
            field = array

            if (!LiquidBounce.isStarting) {
                LiquidBounce.configManager.smartSave()
            }
        }
    val canEnable: Boolean
    var autoDisable: EnumAutoDisableType
    var triggerType: EnumTriggerType
    val moduleCommand: Boolean
    val moduleInfo = javaClass.getAnnotation(ModuleInfo::class.java)!!
    var splicedName = ""
        get() {
//            val translatedName=LanguageManager.replace(localizedName)
//            if(field.replace(" ","") != translatedName){
//                field=StringUtils.toCompleteString(RegexUtils.match(translatedName, "[A-Z][a-z]*"))
//            }
            if (field.isEmpty()) {
                val sb = StringBuilder()
                val arr = name.toCharArray()
                for (i in arr.indices) {
                    val char = arr[i]
                    if (i != 0 && !Character.isLowerCase(char) && Character.isLowerCase(arr[i - 1])) {
                        sb.append(' ')
                    }
                    sb.append(char)
                }
                field = sb.toString()
            }
            return field
        }

    init {
        name = moduleInfo.name
        description = "%module.$name.description%"
        category = moduleInfo.category
        keyBind = moduleInfo.keyBind
        array = moduleInfo.array
        canEnable = moduleInfo.canEnable
        autoDisable = moduleInfo.autoDisable
        moduleCommand = moduleInfo.moduleCommand
        triggerType = moduleInfo.triggerType
    }

    open fun onLoad() {
        localizedName = "%module.$name.name%"
    }

    // Current state of module
    var state = false
        set(value) {
            if (field == value) return

            // Call toggle
            onToggle(value)

            // Play sound and add notification
            if (!LiquidBounce.isStarting) {
                if (value) {
                    Modules.playSound(true)
                    LiquidBounce.hud.addNotification(Notification("%notify.module.title%", LanguageManager.getAndFormat("notify.module.enable", localizedName), NotifyType.SUCCESS))
                } else {
                    Modules.playSound(false)
                    LiquidBounce.hud.addNotification(Notification("%notify.module.title%", LanguageManager.getAndFormat("notify.module.disable", localizedName), NotifyType.ERROR))
                }
            }

            // Call on enabled or disabled
            try {
                if (value) {
                    onEnable()

                    if (canEnable) {
                        field = true
                    }
                } else {
                    onDisable()

                    field = false
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            // Save module state
            LiquidBounce.configManager.smartSave()
        }

    // HUD
    val hue = Math.random().toFloat()
    var slideAnimation: Animation? = null
    var slide = 0f
        get() {
            if (slideAnimation != null) {
                field = slideAnimation!!.value.toFloat()
                if (slideAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    slideAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (slideAnimation == null || (slideAnimation != null && slideAnimation!!.to != value.toDouble())) {
                slideAnimation = Animation(EaseUtils.EnumEasingType.valueOf(HUD.arraylistXAxisAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(HUD.arraylistXAxisAnimOrderValue.get()), field.toDouble(), value.toDouble(), HUD.arraylistXAxisAnimSpeedValue.get() * 30L).start()
            }
        }
    var yPosAnimation: Animation? = null
    var yPos = 0f
        get() {
            if (yPosAnimation != null) {
                field = yPosAnimation!!.value.toFloat()
                if (yPosAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    yPosAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (yPosAnimation == null || (yPosAnimation != null && yPosAnimation!!.to != value.toDouble())) {
                yPosAnimation = Animation(EaseUtils.EnumEasingType.valueOf(HUD.arraylistYAxisAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(HUD.arraylistYAxisAnimOrderValue.get()), field.toDouble(), value.toDouble(), HUD.arraylistYAxisAnimSpeedValue.get() * 30L).start()
            }
        }

    // Tag
    open val tag: String?
        get() = null

    val tagName: String
        get() = "$name${if (tag == null) "" else " §7$tag"}"

    val colorlessTagName: String
        get() = "$name${if (tag == null) "" else " " + stripColor(tag!!)}"

    var width = 10

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Print [msg] to chat as alert
     */
    protected fun alert(msg: String) = ClientUtils.displayAlert(msg)

    /**
     * Print [msg] to chat as plain text
     */
    protected fun chat(msg: String) = ClientUtils.displayChatMessage(msg)

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
     * Called when module initialized
     */
    open fun onInitialize() {}

    /**
     * Get all values of module
     */
    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    /**
     * Get module by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state
}