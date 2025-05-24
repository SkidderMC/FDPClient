/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.file.gson.json
import net.minecraft.client.gui.FontRenderer
import java.awt.Color

/**
 * A container of the values
 */
open class Configurable(
    name: String
) : Value<MutableList<Value<*>>>(
    name, mutableListOf()
) {

    val values: List<Value<*>>
        get() = this.get()

    fun addValue(value: Value<*>) = apply {
        get().add(value)
        value.owner = this
    }

    fun addValues(values: Collection<Value<*>>) = apply {
        get().addAll(values)
        values.forEach { it.owner = this }
    }

    operator fun <T, V : Value<T>> V.unaryPlus() = apply(::addValue)

    override fun toJson(): JsonElement = json {
        for (value in values) {
            if (value.excluded) {
                continue
            }

            value.name to value.toJson()
        }
    }

    override fun fromJsonF(element: JsonElement): MutableList<Value<*>>? {
        element as JsonObject

        val values = get()
        // Set all sub values from the JSON object
        for ((valueName, value) in element.entrySet()) {
            values.find { it.name.equals(valueName, true) }?.fromJson(value)
        }

        return values
    }

    override fun toText(): String {
        TODO("Not yet implemented")
    }

    override fun fromTextF(text: String): MutableList<Value<*>>? {
        TODO("Not yet implemented")
    }

    fun int(
        name: String, value: Int, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +IntValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun float(
        name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +FloatValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun choices(
        name: String, values: Array<String>, value: String, isSupported: (() -> Boolean)? = null
    ) = +ListValue(name, values, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun block(
        name: String, value: Int, isSupported: (() -> Boolean)? = null
    ) = +BlockValue(name, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun font(
        name: String, value: FontRenderer, isSupported: (() -> Boolean)? = null
    ) = +FontValue(name, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun text(
        name: String, value: String, isSupported: (() -> Boolean)? = null
    ) = +TextValue(name, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun boolean(
        name: String, value: Boolean, isSupported: (() -> Boolean)? = null
    ) = +BoolValue(name, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun intRange(
        name: String, value: IntRange, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +IntRangeValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun floatRange(
        name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
        suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +FloatRangeValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun color(
        name: String, value: Color, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null
    ) = +ColorValue(name, value, rainbow).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun color(
        name: String, value: Int, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null
    ) = color(name, Color(value, true), rainbow, isSupported)
}