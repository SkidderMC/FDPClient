/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.file.gson.json
import net.ccbluex.liquidbounce.file.gson.jsonArray
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.kotlin.coerceIn
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.input.Mouse
import java.awt.Color
import javax.vecmath.Vector2f
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

/**
 * Bool value represents a value with a boolean
 */
class BoolValue(
    name: String,
    value: Boolean,
) : Value<Boolean>(name, value) {

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asBoolean || element.asString.equals("true", ignoreCase = true)
            else -> null
        }

    override fun fromTextF(text: String): Boolean? =
        when (text.lowercase()) {
            "true", "t", "yes", "y" -> true
            "false", "f", "no", "n" -> false
            else -> null
        }

    fun toggle() = set(!value)

    fun isActive() = value && shouldRender()

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return super.getValue(thisRef, property) && (shouldRender() || hidden)
    }
}

/**
 * Integer value represents a value with a integer
 */
class IntValue(
    name: String,
    value: Int,
    val range: IntRange,
    suffix: String? = null,
) : Value<Int>(name, value, suffix) {

    override fun validate(newValue: Int): Int = newValue.coerceIn(range)

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asInt
            else -> null
        }

    override fun fromTextF(text: String): Int? = text.toIntOrNull()

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.first
    val maximum = range.last
}

class IntRangeValue(
    name: String,
    value: IntRange,
    val range: IntRange,
    suffix: String? = null,
) : Value<IntRange>(name, value, suffix) {

    override fun validate(newValue: IntRange): IntRange = newValue.coerceIn(range)

    var lastChosenSlider: RangeSlider? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    fun setFirst(newValue: Int, immediate: Boolean = true) = set(newValue..value.last, immediate)
    fun setLast(newValue: Int, immediate: Boolean = true) = set(value.first..newValue, immediate)

    override fun toJson(): JsonElement = jsonArray {
        +JsonPrimitive(value.first)
        +JsonPrimitive(value.last)
    }

    override fun fromJsonF(element: JsonElement): IntRange? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 2 } ?: return null
        return IntRange(array[0].asInt, array[1].asInt)
    }

    override fun fromTextF(text: String): IntRange? {
        val (first, last) = text.split("..").takeIf { it.size == 2 } ?: return null
        return IntRange(first.toInt(), last.toInt())
    }

    fun isMinimal() = value.first <= minimum
    fun isMaximal() = value.last >= maximum

    val minimum = range.first
    val maximum = range.last

    val random
        get() = nextInt(value.first, value.last)
}

/**
 * Float value represents a value with a float
 */
class FloatValue(
    name: String,
    value: Float,
    val range: ClosedFloatingPointRange<Float>,
    suffix: String? = null,
) : Value<Float>(name, value, suffix) {

    override fun validate(newValue: Float): Float = newValue.coerceIn(range)

    fun set(newValue: Number) = set(newValue.toFloat())

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asFloat
            else -> null
        }

    override fun fromTextF(text: String): Float? = text.toFloatOrNull()

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.start
    val maximum = range.endInclusive
}

class FloatRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    val range: ClosedFloatingPointRange<Float>,
    suffix: String? = null,
) : Value<ClosedFloatingPointRange<Float>>(name, value, suffix) {

    override fun validate(newValue: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float> = newValue.coerceIn(range)

    var lastChosenSlider: RangeSlider? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    fun setFirst(newValue: Float, immediate: Boolean = true) = set(newValue..value.endInclusive, immediate)
    fun setLast(newValue: Float, immediate: Boolean = true) = set(value.start..newValue, immediate)

    override fun toJson(): JsonElement = jsonArray {
        +JsonPrimitive(value.start)
        +JsonPrimitive(value.endInclusive)
    }

    override fun fromJsonF(element: JsonElement): ClosedFloatingPointRange<Float>? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 2 } ?: return null
        return array[0].asFloat..array[1].asFloat
    }

    override fun fromTextF(text: String): ClosedFloatingPointRange<Float>? {
        val (first, last) = text.split("..").takeIf { it.size == 2 } ?: return null
        return first.toFloat()..last.toFloat()
    }

    fun isMinimal() = value.start <= minimum
    fun isMaximal() = value.endInclusive >= maximum

    val minimum = range.start
    val maximum = range.endInclusive

    val random
        get() = nextFloat(value.start, value.endInclusive)
}

/**
 * Text value represents a value with a string
 */
class TextValue(
    name: String,
    value: String,
) : Value<String>(name, value) {

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asString
            else -> null
        }

    override fun fromTextF(text: String): String = text

}

/**
 * Font value represents a value with a font
 */
class FontValue(
    name: String,
    value: FontRenderer,
) : Value<FontRenderer>(name, value) {

    override fun toJson(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        return json {
            "fontName" to fontDetails.name
            "fontSize" to fontDetails.size
        }
    }

    override fun fromJsonF(element: JsonElement) =
        when {
            element is JsonObject -> Fonts.getFontRenderer(element["fontName"].asString, element["fontSize"].asInt)
            else -> null
        }

    override fun toText(): String {
        return displayName
    }

    override fun fromTextF(text: String): FontRenderer? {
        // Font values are always excluded from Text configs
        return null
    }

    val displayName
        get() = "Font: " + when (val cur = value) {
            is GameFontRenderer -> "${cur.defaultFont.font.name} - ${cur.defaultFont.font.size}"
            Fonts.minecraftFont -> "Minecraft"
            else -> {
                val fontInfo = Fonts.getFontDetails(cur)
                fontInfo?.let {
                    "${it.name}${if (it.size != -1) " - ${it.size}" else ""}"
                } ?: "Unknown"
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
class BlockValue(
    name: String, value: Int, val range: IntRange = 1..197
) : Value<Int>(name, value, suffix = null) {

    override fun validate(newValue: Int): Int = newValue.coerceIn(range)

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asInt
            else -> null
        }

    override fun fromTextF(text: String): Int? = text.toIntOrNull()

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.first
    val maximum = range.last
}

/**
 * List value represents a selectable list of values
 */
class ListValue(
    name: String,
    var values: Array<String>,
    value: String,
) : Value<String>(name, value) {

    override fun validate(newValue: String): String = values.find { it.equals(newValue, true) } ?: default

    var openList = false

    operator fun contains(string: String?) = values.any { it.equals(string, true) }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> validate(element.asString)
            else -> null
        }

    override fun fromTextF(text: String): String? = values.find { it.equals(text, true) }

    fun updateValues(newValues: Array<String>) {
        values = newValues
    }
}

class ColorValue(
    name: String, defaultColor: Color, var rainbow: Boolean = false
) : Value<Color>(name, defaultColor) {
    // Sliders
    var hueSliderY = 0F
    var opacitySliderY = 0F

    // Slider positions in the 0-1 range
    var colorPickerPos = Vector2f(0f, 0f)

    var showPicker = false

    var showOptions = false

    var rgbaIndex = 0

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
        ClientThemesUtils.getColor().withAlpha((opacitySliderY * 255).roundToInt())
    } else {
        get()
    }

    override fun toJson(): JsonElement {
        val pos = colorPickerPos
        return json {
            "ColorPicker" to jsonArray { +JsonPrimitive(pos.x); +JsonPrimitive(pos.y) }
            "HueSliderY" to hueSliderY
            "OpacitySliderY" to opacitySliderY
            "Rainbow" to rainbow
        }
    }

    override fun fromJsonF(element: JsonElement): Color? =
        when {
            element is JsonObject -> try {
                val colorPickerX = element["ColorPicker"].asJsonArray[0].asFloat
                val colorPickerY = element["ColorPicker"].asJsonArray[1].asFloat
                val hueSliderY = element["HueSliderY"].asFloat
                val opacitySliderY = element["OpacitySliderY"].asFloat
                val rainbowString = element["Rainbow"].asBoolean

                this.colorPickerPos = Vector2f(colorPickerX, colorPickerY)
                this.hueSliderY = hueSliderY
                this.opacitySliderY = opacitySliderY
                this.rainbow = rainbowString

                // Change the current color based on the data from values.json
                Color(
                    Color.HSBtoRGB(this.hueSliderY, colorPickerX, 1 - colorPickerY), true
                ).withAlpha((opacitySliderY * 255).roundToInt())
            } catch (_: Exception) { null }
            else -> null
        }

    override fun toText() =
        "Color(ColorPicker=[${colorPickerPos.x},${colorPickerPos.y}],HueSliderY=${hueSliderY},OpacitySliderY=${(opacitySliderY)},Rainbow=$rainbow)"

    override fun fromTextF(text: String): Color? {
        return try {
            val colorPickerRegex = "ColorPicker=\\[(\\d+\\.?\\d*),(\\d+\\.?\\d*)]".toRegex()
            val colorPickerMatch = colorPickerRegex.find(text)
            val colorPickerX = colorPickerMatch?.groupValues?.get(1)?.toFloat() ?: return null
            val colorPickerY = colorPickerMatch.groupValues[2].toFloat()

            val hueSliderRegex = "HueSliderY=(\\d+\\.?\\d*)".toRegex()
            val hueSliderMatch = hueSliderRegex.find(text)
            val hueSliderY = hueSliderMatch?.groupValues?.get(1)?.toFloat() ?: return null

            val opacitySliderRegex = "OpacitySliderY=(\\d+\\.?\\d*)".toRegex()
            val opacitySliderMatch = opacitySliderRegex.find(text)
            val opacitySliderY = opacitySliderMatch?.groupValues?.get(1)?.toFloat() ?: return null

            val rainbowRegex = "Rainbow=(true|false)".toRegex()
            val rainbowMatch = rainbowRegex.find(text)
            val rainbow = rainbowMatch?.groupValues?.get(1)?.toBoolean() ?: return null

            this.colorPickerPos = Vector2f(colorPickerX, colorPickerY)
            this.hueSliderY = hueSliderY
            this.opacitySliderY = opacitySliderY
            this.rainbow = rainbow

            Color(
                Color.HSBtoRGB(this.hueSliderY, colorPickerX, 1 - colorPickerY), true
            ).withAlpha((opacitySliderY * 255).roundToInt())
        } catch (_: Exception) { null }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Color {
        return selectedColor()
    }

    // Every change that is not coming from any ClickGUI styles should modify the sliders to synchronize with the new color.
    init {
        onChanged(::setupSliders)
    }

    enum class SliderType {
        COLOR, HUE, OPACITY
    }
}

enum class RangeSlider { LEFT, RIGHT }