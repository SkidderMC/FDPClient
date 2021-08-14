/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.ToggleSound
import net.ccbluex.liquidbounce.script.api.ScriptModule
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.Value
import org.lwjgl.input.Keyboard

open class Module : MinecraftInstance(), Listenable {
    // Module information
    var name: String
    var localizedName=""
        get()=field.ifEmpty { name }
    var description: String
    var category: ModuleCategory
    var keyBind = Keyboard.CHAR_NONE
        set(keyBind) {
            field = keyBind

            if (!LiquidBounce.isStarting)
                LiquidBounce.configManager.smartSave()
        }
    var array = true
        set(array) {
            field = array

            if (!LiquidBounce.isStarting)
                LiquidBounce.configManager.smartSave()
        }
    val canEnable: Boolean
    var autoDisable: EnumAutoDisableType
    val moduleCommand: Boolean
    val moduleInfo = javaClass.getAnnotation(ModuleInfo::class.java)!!
    var splicedName=""
        get() {
//            val translatedName=LanguageManager.replace(localizedName)
//            if(field.replace(" ","") != translatedName){
//                field=StringUtils.toCompleteString(RegexUtils.match(translatedName, "[A-Z][a-z]*"))
//            }
            if(field.isEmpty()){
                val sb=StringBuilder()
                val arr=name.toCharArray()
                for(i in arr.indices){
                    val char=arr[i]
                    if(i!=0&&!Character.isLowerCase(char)&&Character.isLowerCase(arr[i-1])){
                        sb.append(' ')
                    }
                    sb.append(char)
                }
                field=sb.toString()
            }
            return field
        }

    var slideStep = 0F

    init {
        name = moduleInfo.name
        description = "%module.$name.description%"
        category = moduleInfo.category
        keyBind = moduleInfo.keyBind
        array = moduleInfo.array
        canEnable = moduleInfo.canEnable
        autoDisable = moduleInfo.autoDisable
        moduleCommand = moduleInfo.moduleCommand
    }

    fun loadLocale(){
        if(this !is ScriptModule){
            localizedName="%module.$name.name%"
            values.forEach {
                it.localedName="%module.$name.value.${it.name}.name%"
            }
        }
    }

    // Current state of module
    var state = moduleInfo.defaultOn
        set(value) {
            if (field == value) return

            // Call toggle
            onToggle(value)

            // Play sound and add notification
            if (!LiquidBounce.isStarting) {
                if(value){
                    ToggleSound.playSound(true)
                    LiquidBounce.hud.addNotification(Notification("%notify.module.title%",LanguageManager.getAndFormat("notify.module.enable",localizedName), NotifyType.SUCCESS))
                }else{
                    ToggleSound.playSound(false)
                    LiquidBounce.hud.addNotification(Notification("%notify.module.title%",LanguageManager.getAndFormat("notify.module.disable",localizedName), NotifyType.ERROR))
                }
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canEnable)
                    field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            LiquidBounce.configManager.smartSave()
        }


    // HUD
    val hue = Math.random().toFloat()
    var slide = 0F

    // Tag
    open val tag: String?
        get() = null

    val tagName: String
        get() = "$name${if (tag == null) "" else " §7$tag"}"

    val colorlessTagName: String
        get() = "$name${if (tag == null) "" else " " + stripColor(tag)}"

    var width=10

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    protected fun chat(msg: String) = ClientUtils.displayAlert(msg)

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
     * Get module by [valueName]
     */
    open fun getValue(valueName: String) = javaClass.declaredFields.map { valueField ->
        valueField.isAccessible = true
        valueField[this]
    }.filterIsInstance<Value<*>>().find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get all values of module
     */
    open val values: List<Value<*>>
        get() = javaClass.declaredFields.map { valueField ->
            valueField.isAccessible = true
            valueField[this]
        }.filterIsInstance<Value<*>>()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state
}