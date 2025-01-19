/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.input.Mouse
import java.awt.Color
import javax.vecmath.Vector2f
import kotlin.math.roundToInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class Value<T>(
    val name: String,
    open var value: T,
    val subjective: Boolean = false,
    var isSupported: (() -> Boolean)? = null,
    val suffix: String? = null,
    protected var default: T = value,
) : ReadWriteProperty<Any?, T> {

    var excluded: Boolean = false
        private set

    var hidden = false
        private set

    fun setAndUpdateDefault(new: T): Boolean {
        default = new

        return set(new)
    }

    fun set(newValue: T, saveImmediately: Boolean = true): Boolean {
        if (newValue == value || hidden || excluded) return false

        val oldValue = value

        try {
            val handledValue = onChange(oldValue, newValue)
            if (handledValue == oldValue) return false

            changeValue(handledValue)
            onChanged(oldValue, handledValue)
            onUpdate(handledValue)

            if (saveImmediately) {
                saveConfig(valuesConfig)
            }
            return true
        } catch (e: Exception) {
            LOGGER.error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
            return false
        }
    }

    /**
     * Use only when you want an option to be hidden while keeping its state.
     *
     * [state] the value it will be set to before it is hidden.
     */
    fun hideWithState(state: T = value) {
        setAndUpdateDefault(state)

        hidden = true
    }

    /**
     * Excludes the chosen option [value] from the config system.
     *
     * [state] the value it will be set to before it is excluded.
     */
    fun excludeWithState(state: T = value) {
        setAndUpdateDefault(state)

        excluded = true
    }

    fun get() = value

    open fun changeValue(newValue: T) {
        value = newValue
    }

    open fun toJson() = toJsonF()

    open fun fromJson(element: JsonElement) {
        val result = fromJsonF(element)
        if (result != null) changeValue(result)

        onInit(value)
        onUpdate(value)
    }

    abstract fun toJsonF(): JsonElement?
    abstract fun fromJsonF(element: JsonElement): T?

    protected open fun onInit(value: T) {}
    protected open fun onUpdate(value: T) {}
    protected open fun onChange(oldValue: T, newValue: T) = newValue
    protected open fun onChanged(oldValue: T, newValue: T) {}
    open fun isSupported() = isSupported?.invoke() != false

    open fun setSupport(condition: (Boolean) -> Boolean) {
        isSupported = { condition(isSupported()) }
    }

    // Support for delegating values using the `by` keyword.
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(value)
    }

    open fun getString() = "$value"

    fun shouldRender() = isSupported() && !hidden

    fun reset() = set(default)
}

/**
 * Bool value represents a value with a boolean
 */
open class BoolValue(
    name: String,
    value: Boolean,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<Boolean>(name, value, subjective, isSupported) {

    override fun toJsonF() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        if (element.isJsonPrimitive) element.asBoolean || element.asString.equals("true", ignoreCase = true)
        else null

    fun toggle() = set(!value)

    fun isActive() = value && (isSupported() || hidden)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return super.getValue(thisRef, property) && isActive()
    }
}

/**
 * Integer value represents a value with a integer
 */
open class IntegerValue(
    name: String,
    value: Int,
    val range: IntRange = 0..Int.MAX_VALUE,
    suffix: String? = null,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<Int>(name, value, subjective, isSupported, suffix) {

    fun set(newValue: Number) = set(newValue.toInt())

    override fun toJsonF() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = if (element.isJsonPrimitive) element.asInt else null

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.first
    val maximum = range.last
}

// TODO: Replace Min/Max options with this instead
open class IntegerRangeValue(
    name: String,
    value: IntRange,
    val range: IntRange = 0..Int.MAX_VALUE,
    suffix: String? = null,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<IntRange>(name, value, subjective, isSupported, suffix) {

    var lastChosenSlider: RangeSlider? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    fun setFirst(newValue: Int, immediate: Boolean = true) = set(newValue..value.last, immediate)
    fun setLast(newValue: Int, immediate: Boolean = true) = set(value.first..newValue, immediate)

    override fun toJsonF(): JsonElement {
        return JsonPrimitive("${value.first}-${value.last}")
    }

    override fun fromJsonF(element: JsonElement): IntRange? {
        return element.asJsonPrimitive?.asString?.split("-")?.takeIf { it.size == 2 }?.let {
            val (start, end) = it

            start.toIntOrNull()?.let { s ->
                end.toIntOrNull()?.let { e ->
                    s..e
                }
            }
        }
    }

    fun isMinimal() = value.first <= minimum
    fun isMaximal() = value.last >= maximum

    val minimum = range.first
    val maximum = range.last

    val random
        get() = nextInt(value.first, value.last)
}

// TODO: Replace Min/Max options with this instead
open class FloatRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    val range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    suffix: String? = null,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<ClosedFloatingPointRange<Float>>(name, value, subjective, isSupported, suffix) {

    var lastChosenSlider: RangeSlider? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    fun setFirst(newValue: Float, immediate: Boolean = true) = set(newValue..value.endInclusive, immediate)
    fun setLast(newValue: Float, immediate: Boolean = true) = set(value.start..newValue, immediate)

    override fun toJsonF(): JsonElement {
        return JsonPrimitive("${value.start}-${value.endInclusive}")
    }

    override fun fromJsonF(element: JsonElement): ClosedFloatingPointRange<Float>? {
        return element.asJsonPrimitive?.asString?.split("-")?.takeIf { it.size == 2 }?.let {
            val (start, end) = it

            start.toFloatOrNull()?.let { s ->
                end.toFloatOrNull()?.let { e ->
                    s..e
                }
            }
        }
    }

    fun isMinimal() = value.start <= minimum
    fun isMaximal() = value.endInclusive >= maximum

    val minimum = range.start
    val maximum = range.endInclusive

    val random
        get() = nextFloat(value.start, value.endInclusive)

}

/**
 * Float value represents a value with a float
 */
open class FloatValue(
    name: String,
    value: Float,
    val range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    suffix: String? = null,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<Float>(name, value, subjective, isSupported, suffix) {

    fun set(newValue: Number) = set(newValue.toFloat())

    override fun toJsonF() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = if (element.isJsonPrimitive) element.asFloat else null

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.start
    val maximum = range.endInclusive
}

/**
 * Text value represents a value with a string
 */
open class TextValue(
    name: String,
    value: String,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<String>(name, value, subjective, isSupported) {

    override fun toJsonF() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = if (element.isJsonPrimitive) element.asString else null
}

/**
 * Font value represents a value with a font
 */
open class FontValue(
    name: String,
    value: FontRenderer,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<FontRenderer>(name, value, subjective, isSupported) {

    override fun toJsonF(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        val valueObject = JsonObject()
        valueObject.run {
            addProperty("fontName", fontDetails.name)
            addProperty("fontSize", fontDetails.size)
        }
        return valueObject
    }

    override fun fromJsonF(element: JsonElement) = if (element.isJsonObject) {
        val valueObject = element.asJsonObject
        Fonts.getFontRenderer(valueObject["fontName"].asString, valueObject["fontSize"].asInt)
    } else null

    val displayName
        get() = when (value) {
            is GameFontRenderer -> "Font: ${(value as GameFontRenderer).defaultFont.font.name} - ${(value as GameFontRenderer).defaultFont.font.size}"
            Fonts.minecraftFont -> "Font: Minecraft"
            else -> {
                val fontInfo = Fonts.getFontDetails(value)
                fontInfo?.let {
                    "${it.name}${if (it.size != -1) " - ${it.size}" else ""}"
                } ?: "Font: Unknown"
            }
        }

    fun next() {
        val fonts = Fonts.fonts
        value = fonts[(fonts.indexOf(value) + 1) % fonts.size]
    }

    fun previous() {
        val fonts = Fonts.fonts
        value = fonts[(fonts.indexOf(value) - 1 + fonts.size) % fonts.size]
    }
}

/**
 * Block value represents a value with a block
 */
open class BlockValue(
    name: String, value: Int, subjective: Boolean = false, isSupported: (() -> Boolean)? = null,
) : IntegerValue(name, value, 1..197, null, subjective, isSupported)

/**
 * List value represents a selectable list of values
 */
open class ListValue(
    name: String,
    var values: Array<String>,
    override var value: String,
    subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null,
) : Value<String>(name, value, subjective, isSupported) {

    var openList = false

    operator fun contains(string: String?) = values.any { it.equals(string, true) }

    override fun changeValue(newValue: String) {
        values.find { it.equals(newValue, true) }?.let { value = it }
    }

    override fun toJsonF() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = if (element.isJsonPrimitive) element.asString else null

    fun updateValues(newValues: Array<String>) {
        values = newValues
    }
}

open class ColorValue(
    name: String, defaultColor: Color, var rainbow: Boolean = false, var showPicker: Boolean = false,
    subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) : Value<Color>(name, defaultColor, subjective = subjective, isSupported = isSupported) {
    // Sliders
    var hueSliderY = 0F
    var opacitySliderY = 0F

    // Slider positions in the 0-1 range
    var colorPickerPos = Vector2f(0f, 0f)

    var lastChosenSlider: SliderType? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    init {
        setupSliders(defaultColor)
    }

    fun setupSliders(color: Color) {
        Color.RGBtoHSB(color.red, color.green, color.blue, null).also {
            hueSliderY = it[0]
            opacitySliderY = color.alpha / 255f
            colorPickerPos.set(it[1], 1 - it[2])
        }
    }

    fun selectedColor() = if (rainbow) {
        ColorUtils.rainbow(alpha = opacitySliderY)
    } else {
        get()
    }

    override fun toJsonF(): JsonElement {
        val pos = colorPickerPos
        return JsonPrimitive("colorpicker: [${pos.x}, ${pos.y}], hueslider: ${hueSliderY}, opacity: ${opacitySliderY}, rainbow: $rainbow")
    }

    override fun fromJsonF(element: JsonElement): Color? {
        if (element.isJsonPrimitive) {
            val raw = element.asString

            val regex =
                """colorpicker:\s*\[\s*(-?\d*\.?\d+),\s*(-?\d*\.?\d+)\s*],\s*hueslider:\s*(-?\d*\.?\d+),\s*opacity:\s*(-?\d*\.?\d+),\s*rainbow:\s*(true|false)""".toRegex()
            val matchResult = regex.find(raw)

            if (matchResult != null) {
                val colorPickerX = matchResult.groupValues[1].toFloatOrNull()
                val colorPickerY = matchResult.groupValues[2].toFloatOrNull()
                val hueSliderY = matchResult.groupValues[3].toFloatOrNull()
                val opacitySliderY = matchResult.groupValues[4].toFloatOrNull()
                val rainbowString = matchResult.groupValues[5].toBoolean()

                if (colorPickerX != null && colorPickerY != null && hueSliderY != null && opacitySliderY != null) {
                    colorPickerPos = Vector2f(colorPickerX, colorPickerY)
                    this.hueSliderY = hueSliderY
                    this.opacitySliderY = opacitySliderY
                    this.rainbow = rainbowString

                    // Change the current color based on the data from values.json
                    return Color(
                        Color.HSBtoRGB(this.hueSliderY, colorPickerX, 1 - colorPickerY), true
                    ).withAlpha((opacitySliderY * 255).roundToInt())
                }
            }
        }
        return null
    }

    override fun getString() =
        "Color[picker=[${colorPickerPos.x},${colorPickerPos.y}],hueslider=${hueSliderY},opacity=${(opacitySliderY)},rainbow=$rainbow]"

    override fun getValue(thisRef: Any?, property: KProperty<*>): Color {
        return selectedColor()
    }

    // Every change that is not coming from any ClickGUI styles should modify the sliders to synchronize with the new color.
    override fun onChanged(oldValue: Color, newValue: Color) {
        setupSliders(newValue)
    }

    fun readColorFromConfig(str: String): List<String>? {
        val regex =
            """Color\[picker=\[\s*(-?\d*\.?\d+),\s*(-?\d*\.?\d+)],\s*hueslider=\s*(-?\d*\.?\d+),\s*opacity=\s*(-?\d*\.?\d+),\s*rainbow=(true|false)]""".toRegex()
        val matchResult = regex.find(str)

        return matchResult?.groupValues?.drop(1)
    }

    enum class SliderType {
        COLOR, HUE, OPACITY
    }
}

fun int(
    name: String, value: Int, range: IntRange = 0..Int.MAX_VALUE, suffix: String? = null, subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null
) = IntegerValue(name, value, range, suffix, subjective, isSupported)

fun float(
    name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null,
    subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = FloatValue(name, value, range, suffix, subjective, isSupported)

fun choices(
    name: String, values: Array<String>, value: String, subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null
) = ListValue(name, values, value, subjective, isSupported)

fun block(
    name: String, value: Int, subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = BlockValue(name, value, subjective, isSupported)

fun font(
    name: String, value: FontRenderer, subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = FontValue(name, value, subjective, isSupported)

fun text(
    name: String, value: String, subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = TextValue(name, value, subjective, isSupported)

fun boolean(
    name: String, value: Boolean, subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = BoolValue(name, value, subjective, isSupported)

fun intRange(
    name: String, value: IntRange, range: IntRange = 0..Int.MAX_VALUE, suffix: String? = null,
    subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = IntegerRangeValue(name, value, range, suffix, subjective, isSupported)

fun floatRange(
    name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    suffix: String? = null, subjective: Boolean = false, isSupported: (() -> Boolean)? = null
) = FloatRangeValue(name, value, range, suffix, subjective, isSupported)

fun color(
    name: String, value: Color, rainbow: Boolean = false, showPicker: Boolean = false, subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null
) = ColorValue(name, value, rainbow, showPicker, subjective, isSupported)

fun color(
    name: String, value: Int, rainbow: Boolean = false, showPicker: Boolean = false, subjective: Boolean = false,
    isSupported: (() -> Boolean)? = null
) = color(name, Color(value, true), rainbow, showPicker, subjective, isSupported)

enum class RangeSlider { LEFT, RIGHT }