/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient
 */
package net.ccbluex.liquidbounce.features.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.awt.Color
import kotlin.jvm.internal.Intrinsics

abstract class Value<T>(val name: String, var value: T) {
    val default = value
    var textHovered: Boolean = false

    private var displayableFunc: () -> Boolean = { true }

    fun displayable(func: () -> Boolean): Value<T> {
        displayableFunc = func
        return this
    }

    val displayable: Boolean
        get() = displayableFunc()

    val displayableFunction: () -> Boolean
        get() = displayableFunc

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
            FDPClient.configManager.smartSave()
        } catch (e: Exception) {
            ClientUtils.logError("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }



    fun get() = value

    fun setDefault() {
        value = default
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

    // this is better api for ListValue and TextValue

    open class ColorValue(name: String, value: Int, canDisplay: () -> Boolean) : Value<Int>(name, value) {
        val minimum: Int = -10000000
        val maximum: Int = 1000000
        fun set(newValue: Number) {
            set(newValue.toInt())
        }
        override fun toJson() = JsonPrimitive(value)
        override fun fromJson(element: JsonElement) {
            if (element.isJsonPrimitive)
                value = element.asInt
        }
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (value is String && other is String) {
            return (value as String).equals(other, true)
        }
        return value?.equals(other) ?: false
    }

    fun contains(text: String/*, ignoreCase: Boolean*/): Boolean {
        return if (value is String) {
            (value as String).contains(text, true)
        } else {
            false
        }
    }

    private var Expanded = false

    open fun getExpanded(): Boolean {
        return Expanded
    }

    open fun setExpanded(b: Boolean) {
        this.Expanded
    }

    open fun isExpanded(): Boolean {
        return Expanded
    }


    open fun getAwtColor(): Color {
        return Color((this as Value<Number>).value.toInt(), true)
    }

    open fun ColorValue(name: String, value: Int) {
        Intrinsics.checkParameterIsNotNull(name, "name")
        ColorValue(name, value)
    }
}
