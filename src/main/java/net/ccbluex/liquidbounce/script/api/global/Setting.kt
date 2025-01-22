/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.api.global

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptObjectMirror
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.config.*
import net.minecraft.client.gui.FontRenderer

/**
 * Object used by the script API to provide an idiomatic way of creating module values.
 * TODO: intRange, floatRange, color
 *
 * Note: this usage of [Configurable] is incorrect!!
 */
object Setting : Configurable("ScriptSetting") {

    /**
     * Creates a boolean value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [BoolValue]
     */
    @JvmStatic
    fun boolean(settingInfo: JSObject): BoolValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"] as Boolean

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return boolean(name, default) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new) as? Boolean ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as BoolValue
    }

    /**
     * Creates an integer value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [IntValue]
     */
    @JvmStatic
    fun integer(settingInfo: JSObject): IntValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"]!!.toInt()
        val min = settingInfo["min"]!!.toInt()
        val max = settingInfo["max"]!!.toInt()

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return int(name, default, min..max) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new)?.toInt() ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as IntValue
    }

    /**
     * Creates a float value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [FloatValue]
     */
    @JvmStatic
    fun float(settingInfo: JSObject): FloatValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"]!!.toFloat()
        val min = settingInfo["min"]!!.toFloat()
        val max = settingInfo["max"]!!.toFloat()

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return float(name, default, min..max) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new)?.toFloat() ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as FloatValue
    }

    /**
     * Creates a text value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [TextValue]
     */
    @JvmStatic
    fun text(settingInfo: JSObject): TextValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"] as String

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return text(name, default) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new) as? String ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as TextValue
    }

    /**
     * Creates a block value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [BlockValue]
     */
    @JvmStatic
    fun block(settingInfo: JSObject): BlockValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"]!!.toInt()

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return block(name, default) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new)?.toInt() ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as BlockValue
    }

    /**
     * Creates a list value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [ListValue]
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun list(settingInfo: JSObject): ListValue {
        val name = settingInfo["name"] as String
        val values = ScriptUtils.convert(settingInfo["values"], Array<String>::class.java) as Array<String>
        val default = settingInfo["default"] as String

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return choices(name, values, default) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new) as? String ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as ListValue
    }

    /**
     * Creates a font value.
     * @param settingInfo JavaScript object containing information about the value.
     * @return An instance of [FontValue]
     */
    @JvmStatic
    fun font(settingInfo: JSObject): FontValue {
        val name = settingInfo["name"] as String
        val default = settingInfo["default"] as? FontRenderer ?: Fonts.minecraftFont

        val isSupportedCallback = settingInfo["isSupported"] as? ScriptObjectMirror
        val onChangeCallback = settingInfo["onChange"] as? ScriptObjectMirror
        val onChangedCallback = settingInfo["onChanged"] as? ScriptObjectMirror

        return font(name, default) {
            isSupportedCallback?.call(null) as? Boolean ?: true
        }.onChange { old, new ->
            onChangeCallback?.call(null, old, new) as? FontRenderer ?: new
        }.onChanged { new ->
            onChangedCallback?.call(null, new)
        } as FontValue
    }

}

private fun Any.toInt() = (this as Number).toInt()
private fun Any.toFloat() = (this as Number).toFloat()

private operator fun JSObject.get(key: String) =
    if (this.hasMember(key)) this.getMember(key) else null