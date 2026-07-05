/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config
import net.ccbluex.liquidbounce.utils.input.safeKeyName
import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.file.gson.asBooleanOrNull
import net.ccbluex.liquidbounce.file.gson.asFloatOrNull
import net.ccbluex.liquidbounce.file.gson.asIntOrNull
import net.ccbluex.liquidbounce.file.gson.asStringOrNull
import net.ccbluex.liquidbounce.file.gson.json
import net.ccbluex.liquidbounce.file.gson.jsonArray
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.kotlin.coerceIn
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.features.module.modules.other.UnlimitedValues
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import javax.vecmath.Vector2f
import kotlin.math.roundToInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Shared validation for numeric values: keep [newValue] untouched while unlimited values are
 * active, otherwise restrict it to the allowed range via [coerce].
 */
private inline fun <T> coerceUnlessUnlimited(newValue: T, coerce: (T) -> T): T =
    if (UnlimitedValues.handleEvents() && UnlimitedValues.removeLimits) newValue else coerce(newValue)

/**
 * Slider selection that is automatically released (set back to null) once the primary
 * mouse button is no longer held. Shared by the value types that expose a draggable slider.
 */
private class MouseHeldSlider<T> : ReadWriteProperty<Any?, T?> {
    private var current: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (!Mouse.isButtonDown(0)) current = null
        return current
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        current = value
    }
}

/**
 * Bool value represents a value with a boolean
 */
class BoolValue(
    name: String,
    value: Boolean,
) : Value<Boolean>(name, value) {

    override fun describe(text: String): BoolValue = apply { descriptionField = text }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        element.asBooleanOrNull() ?: element.asStringOrNull()?.equals("true", ignoreCase = true)

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

    override fun describe(text: String): IntValue = apply { descriptionField = text }

    override fun validate(newValue: Int): Int = coerceUnlessUnlimited(newValue) { it.coerceIn(range) }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = element.asIntOrNull()

    override fun fromTextF(text: String): Int? = text.toIntOrNull()

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.first
    val maximum = range.last
}

class LongValue(
    name: String,
    value: Long,
    val range: LongRange,
    suffix: String? = null,
) : Value<Long>(name, value, suffix) {
    override fun describe(text: String): LongValue = apply { descriptionField = text }
    override fun validate(newValue: Long) = coerceUnlessUnlimited(newValue) { it.coerceIn(range) }
    override fun toJson() = JsonPrimitive(value)
    override fun fromJsonF(element: JsonElement) = runCatching { element.asLong }.getOrNull()
    override fun fromTextF(text: String) = text.toLongOrNull()
    val minimum = range.first
    val maximum = range.last
}

class LongRangeValue(
    name: String,
    value: LongRange,
    val range: LongRange,
    suffix: String? = null,
) : Value<LongRange>(name, value, suffix) {
    override fun describe(text: String): LongRangeValue = apply { descriptionField = text }
    override fun validate(newValue: LongRange): LongRange {
        require(newValue.first <= newValue.last) { "Range start must not exceed its end" }
        return coerceUnlessUnlimited(newValue) {
            it.first.coerceIn(range)..it.last.coerceIn(range)
        }
    }
    override fun toJson(): JsonElement = jsonArray { +JsonPrimitive(value.first); +JsonPrimitive(value.last) }
    override fun fromJsonF(element: JsonElement): LongRange? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 2 } ?: return null
        return array[0].asLong..array[1].asLong
    }
    override fun fromTextF(text: String): LongRange? {
        val parts = text.split("..").takeIf { it.size == 2 } ?: return null
        return (parts[0].toLongOrNull() ?: return null)..(parts[1].toLongOrNull() ?: return null)
    }
    val minimum = range.first
    val maximum = range.last
}

class IntRangeValue(
    name: String,
    value: IntRange,
    val range: IntRange,
    suffix: String? = null,
) : Value<IntRange>(name, value, suffix) {

    override fun describe(text: String): IntRangeValue = apply { descriptionField = text }

    override fun validate(newValue: IntRange): IntRange {
        require(newValue.first <= newValue.last) { "Range start must not exceed its end" }
        return coerceUnlessUnlimited(newValue) { it.coerceIn(range) }
    }

    var lastChosenSlider: RangeSlider? by MouseHeldSlider()

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

    override fun describe(text: String): FloatValue = apply { descriptionField = text }

    override fun validate(newValue: Float): Float {
        require(newValue.isFinite()) { "Value must be finite" }
        return coerceUnlessUnlimited(newValue) { it.coerceIn(range) }
    }

    fun set(newValue: Number) = set(newValue.toFloat())

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = element.asFloatOrNull()

    override fun fromTextF(text: String): Float? = text.toFloatOrNull()

    fun isMinimal() = value <= minimum
    fun isMaximal() = value >= maximum

    val minimum = range.start
    val maximum = range.endInclusive
}

class DoubleValue(
    name: String,
    value: Double,
    val range: ClosedFloatingPointRange<Double>,
    suffix: String? = null,
) : Value<Double>(name, value, suffix) {
    override fun describe(text: String): DoubleValue = apply { descriptionField = text }
    override fun validate(newValue: Double): Double {
        require(newValue.isFinite()) { "Value must be finite" }
        return coerceUnlessUnlimited(newValue) { it.coerceIn(range) }
    }
    override fun toJson() = JsonPrimitive(value)
    override fun fromJsonF(element: JsonElement) = runCatching { element.asDouble }.getOrNull()
    override fun fromTextF(text: String) = text.toDoubleOrNull()
    val minimum = range.start
    val maximum = range.endInclusive
}

class DoubleRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Double>,
    val range: ClosedFloatingPointRange<Double>,
    suffix: String? = null,
) : Value<ClosedFloatingPointRange<Double>>(name, value, suffix) {
    override fun describe(text: String): DoubleRangeValue = apply { descriptionField = text }
    override fun validate(newValue: ClosedFloatingPointRange<Double>): ClosedFloatingPointRange<Double> {
        require(newValue.start.isFinite() && newValue.endInclusive.isFinite()) { "Range bounds must be finite" }
        require(newValue.start <= newValue.endInclusive) { "Range start must not exceed its end" }
        return coerceUnlessUnlimited(newValue) {
            it.start.coerceIn(range)..it.endInclusive.coerceIn(range)
        }
    }
    override fun toJson(): JsonElement = jsonArray { +JsonPrimitive(value.start); +JsonPrimitive(value.endInclusive) }
    override fun fromJsonF(element: JsonElement): ClosedFloatingPointRange<Double>? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 2 } ?: return null
        return array[0].asDouble..array[1].asDouble
    }
    override fun fromTextF(text: String): ClosedFloatingPointRange<Double>? {
        val parts = text.split("..").takeIf { it.size == 2 } ?: return null
        return (parts[0].toDoubleOrNull() ?: return null)..(parts[1].toDoubleOrNull() ?: return null)
    }
    val minimum = range.start
    val maximum = range.endInclusive
}

open class FloatRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    val range: ClosedFloatingPointRange<Float>,
    suffix: String? = null,
) : Value<ClosedFloatingPointRange<Float>>(name, value, suffix) {

    override fun describe(text: String): FloatRangeValue = apply { descriptionField = text }

    override fun validate(newValue: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float> {
        require(newValue.start.isFinite() && newValue.endInclusive.isFinite()) { "Range bounds must be finite" }
        require(newValue.start <= newValue.endInclusive) { "Range start must not exceed its end" }
        return coerceUnlessUnlimited(newValue) { it.coerceIn(range) }
    }

    var lastChosenSlider: RangeSlider? by MouseHeldSlider()

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

    override fun describe(text: String): TextValue = apply { descriptionField = text }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asString
            else -> null
        }

    override fun fromTextF(text: String): String = text

}

/**
 * File value represents a value holding a filesystem path as a string.
 */
class FileValue(
    name: String,
    value: String = "",
    var dialogMode: FileDialogMode = FileDialogMode.OPEN_FILE,
    var extensions: List<String> = emptyList(),
) : Value<String>(name, value) {

    override fun describe(text: String): FileValue = apply { descriptionField = text }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) =
        when {
            element.isJsonPrimitive -> element.asString
            else -> null
        }

    override fun fromTextF(text: String): String = text

    val shortName: String
        get() {
            if (value.isEmpty()) return "..."
            val slash = value.lastIndexOf('/')
            val backslash = value.lastIndexOf('\\')
            val cut = maxOf(slash, backslash)
            return if (cut >= 0 && cut < value.length - 1) value.substring(cut + 1) else value
        }

    fun openDialog() {
        try {
            SwingUtilities.invokeLater {
                try {
                    val chooser = JFileChooser()
                    chooser.fileSelectionMode = if (dialogMode == FileDialogMode.SELECT_FOLDER) {
                        JFileChooser.DIRECTORIES_ONLY
                    } else {
                        JFileChooser.FILES_ONLY
                    }

                    if (value.isNotEmpty()) {
                        val current = File(value)
                        val start = if (current.isDirectory) current else current.parentFile
                        if (start != null && start.exists()) chooser.currentDirectory = start
                    }

                    if (dialogMode != FileDialogMode.SELECT_FOLDER && extensions.isNotEmpty()) {
                        chooser.isAcceptAllFileFilterUsed = true
                        chooser.addChoosableFileFilter(
                            FileNameExtensionFilter(extensions.joinToString(", "), *extensions.toTypedArray())
                        )
                    }

                    val frame = JFrame()
                    frame.isVisible = true
                    frame.toFront()
                    frame.isVisible = false

                    val result = if (dialogMode == FileDialogMode.SAVE_FILE) {
                        chooser.showSaveDialog(frame)
                    } else {
                        chooser.showOpenDialog(frame)
                    }
                    frame.dispose()

                    if (result == JFileChooser.APPROVE_OPTION) {
                        chooser.selectedFile?.let { set(it.absolutePath) }
                    }
                } catch (ignored: Throwable) {
                }
            }
        } catch (ignored: Throwable) {
        }
    }
}

/**
 * Font value represents a value with a font
 */
class FontValue(
    name: String,
    value: FontRenderer,
) : Value<FontRenderer>(name, value) {

    override fun describe(text: String): FontValue = apply { descriptionField = text }

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

    override fun describe(text: String): BlockValue = apply { descriptionField = text }

    override fun validate(newValue: Int): Int = coerceUnlessUnlimited(newValue) { it.coerceIn(range) }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJsonF(element: JsonElement) = element.asIntOrNull()

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

    override fun describe(text: String): ListValue = apply { descriptionField = text }

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

/** An ordered, directly editable list of strings. */
class MutableListValue(
    name: String,
    value: List<String> = emptyList(),
) : Value<List<String>>(name, value.toList()) {
    override fun describe(text: String): MutableListValue = apply { descriptionField = text }
    override fun validate(newValue: List<String>) = newValue.toList()
    fun add(entry: String, saveImmediately: Boolean = true) = set(value + entry, saveImmediately)
    fun removeAt(index: Int, saveImmediately: Boolean = true) =
        if (index in value.indices) set(value.filterIndexed { i, _ -> i != index }, saveImmediately) else false
    fun update(index: Int, entry: String, saveImmediately: Boolean = true): Boolean {
        if (index !in value.indices) return false
        return set(value.toMutableList().apply { this[index] = entry }, saveImmediately)
    }
    override fun toJson(): JsonElement = jsonArray { value.forEach { +JsonPrimitive(it) } }
    override fun fromJsonF(element: JsonElement): List<String>? =
        (element as? JsonArray)?.mapNotNull { it.asStringOrNull() }
    override fun toText() = value.joinToString("\n")
    override fun fromTextF(text: String) = text.lines()
}

class ColorValue(
    name: String, defaultColor: Color, var rainbow: Boolean = false
) : Value<Color>(name, defaultColor) {

    override fun describe(text: String): ColorValue = apply { descriptionField = text }
    // Sliders
    var hueSliderY = 0F
    var opacitySliderY = 0F

    // Slider positions in the 0-1 range
    var colorPickerPos = Vector2f(0f, 0f)

    var showPicker = false

    var showOptions = false

    var rgbaIndex = 0

    var lastChosenSlider: SliderType? by MouseHeldSlider()

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

/**
 * Multi select value represents a set of chosen options out of a fixed list of [choices].
 */
open class MultiSelectValue(
    name: String,
    value: Set<String>,
    val choices: Array<String>,
) : Value<Set<String>>(name, value) {

    override fun describe(text: String): MultiSelectValue = apply { descriptionField = text }

    var openList = false

    override fun validate(newValue: Set<String>): Set<String> =
        newValue.mapNotNull { selected -> choices.find { it.equals(selected, true) } }.toSet()

    fun isSelected(choice: String) = value.any { it.equals(choice, true) }

    fun toggle(choice: String) {
        val known = choices.find { it.equals(choice, true) } ?: return
        val updated = if (isSelected(known)) {
            value.filterNot { it.equals(known, true) }.toSet()
        } else {
            value + known
        }
        set(updated)
    }

    override fun toJson(): JsonElement = jsonArray {
        for (selected in value) {
            +JsonPrimitive(selected)
        }
    }

    override fun fromJsonF(element: JsonElement): Set<String>? {
        val array = element as? JsonArray ?: return null
        return array.mapNotNull { it.asStringOrNull() }.toSet()
    }

    override fun toText(): String = value.joinToString(",")

    override fun fromTextF(text: String): Set<String> =
        validate(text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet())
}

/** Multi-select backed by a stable string id -> game registry object map. */
class RegistryMultiSelectValue<T : Any>(
    name: String,
    default: Set<String>,
    val registry: Map<String, T>,
) : MultiSelectValue(name, default, registry.keys.toTypedArray()) {

    val selectedEntries: Map<String, T>
        get() = get().mapNotNull { selected ->
            registry.entries.firstOrNull { it.key.equals(selected, true) }?.let { it.key to it.value }
        }.toMap()

    fun resolve(id: String): T? = registry.entries.firstOrNull { it.key.equals(id, true) }?.value
}

/**
 * Key bind value represents a single bound keyboard key.
 */
class KeyBindValue(
    name: String,
    value: Int = Keyboard.KEY_NONE,
    action: KeyBindActionMode = KeyBindActionMode.TOGGLE,
) : Value<Int>(name, value) {

    var actionMode: KeyBindActionMode = action
        private set

    override fun describe(text: String): KeyBindValue = apply { descriptionField = text }

    val keyName: String
        get() = (safeKeyName(value) ?: "None") ?: "None"

    fun setActionMode(action: KeyBindActionMode): Boolean {
        if (actionMode == action) return false
        actionMode = action
        net.ccbluex.liquidbounce.file.FileManager.saveConfig(net.ccbluex.liquidbounce.file.FileManager.valuesConfig)
        owner?.let { ClientChangeBus.publish(ClientChange.ValueState(it.name, name)) }
        return true
    }

    override fun toJson(): JsonElement = json {
        "key" to value
        "action" to actionMode.name
    }

    override fun fromJsonF(element: JsonElement): Int? = when (element) {
        is JsonObject -> {
            element["action"]?.asStringOrNull()?.let { action ->
                KeyBindActionMode.values().firstOrNull { it.name.equals(action, true) }?.let { actionMode = it }
            }
            element["key"]?.asIntOrNull()
        }
        else -> element.asIntOrNull()
    }

    override fun toText(): String = keyName

    override fun fromTextF(text: String): Int? {
        if (text.equals("None", true)) return Keyboard.KEY_NONE
        val index = Keyboard.getKeyIndex(text.uppercase())
        return if (index != Keyboard.KEY_NONE) index else null
    }
}

/**
 * Vec3 value represents three doubles (x, y, z).
 */
class Vec2Value(
    name: String,
    value: DoubleArray,
    val useLocateButton: Boolean = false,
) : Value<DoubleArray>(name, value) {

    override fun describe(text: String): Vec2Value = apply { descriptionField = text }

    init {
        require(value.size == 2) { "Vec2Value requires exactly 2 components" }
    }

    override fun validate(newValue: DoubleArray): DoubleArray {
        require(newValue.size == 2) { "Vec2Value requires exactly 2 components" }
        require(newValue.all(Double::isFinite)) { "Vector components must be finite" }
        return newValue
    }

    var x: Double
        get() = value[0]
        set(new) { set(doubleArrayOf(new, value[1])) }

    var y: Double
        get() = value[1]
        set(new) { set(doubleArrayOf(value[0], new)) }

    override fun toJson(): JsonElement = jsonArray {
        +JsonPrimitive(value[0])
        +JsonPrimitive(value[1])
    }

    override fun fromJsonF(element: JsonElement): DoubleArray? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 2 } ?: return null
        return doubleArrayOf(array[0].asDouble, array[1].asDouble)
    }

    override fun toText(): String = "${value[0]},${value[1]}"

    override fun fromTextF(text: String): DoubleArray? {
        val parts = text.split(",").map(String::trim).takeIf { it.size == 2 } ?: return null
        return doubleArrayOf(parts[0].toDoubleOrNull() ?: return null, parts[1].toDoubleOrNull() ?: return null)
    }
}

class Vec3Value(
    name: String,
    value: DoubleArray,
    val useLocateButton: Boolean = false,
) : Value<DoubleArray>(name, value) {

    override fun describe(text: String): Vec3Value = apply { descriptionField = text }

    init {
        require(value.size == 3) { "Vec3Value requires exactly 3 components" }
    }

    override fun validate(newValue: DoubleArray): DoubleArray {
        require(newValue.size == 3) { "Vec3Value requires exactly 3 components" }
        require(newValue.all(Double::isFinite)) { "Vector components must be finite" }
        return newValue
    }

    var x: Double
        get() = value[0]
        set(new) { set(doubleArrayOf(new, value[1], value[2])) }

    var y: Double
        get() = value[1]
        set(new) { set(doubleArrayOf(value[0], new, value[2])) }

    var z: Double
        get() = value[2]
        set(new) { set(doubleArrayOf(value[0], value[1], new)) }

    override fun toJson(): JsonElement = jsonArray {
        +JsonPrimitive(value[0])
        +JsonPrimitive(value[1])
        +JsonPrimitive(value[2])
    }

    override fun fromJsonF(element: JsonElement): DoubleArray? {
        val array = (element as? JsonArray)?.takeIf { it.size() == 3 } ?: return null
        return doubleArrayOf(array[0].asDouble, array[1].asDouble, array[2].asDouble)
    }

    override fun toText(): String = "${value[0]},${value[1]},${value[2]}"

    override fun fromTextF(text: String): DoubleArray? {
        val parts = text.split(",").map { it.trim() }.takeIf { it.size == 3 } ?: return null
        val x = parts[0].toDoubleOrNull() ?: return null
        val y = parts[1].toDoubleOrNull() ?: return null
        val z = parts[2].toDoubleOrNull() ?: return null
        return doubleArrayOf(x, y, z)
    }
}

/**
 * A tunable curve stored as evenly-spaced control points (each in 0..1). [sample] returns the
 * interpolated height at a position t (0..1). Used for easing/smoothing/animation tuning.
 */
class CurveValue(
    name: String,
    value: DoubleArray = doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0),
) : Value<DoubleArray>(name, value) {

    override fun describe(text: String): CurveValue = apply { descriptionField = text }

    init {
        require(value.size >= 2) { "CurveValue requires at least 2 points" }
    }

    override fun validate(newValue: DoubleArray): DoubleArray {
        require(newValue.size >= 2) { "CurveValue requires at least 2 points" }
        require(newValue.all(Double::isFinite)) { "Curve points must be finite" }
        return DoubleArray(newValue.size) { newValue[it].coerceIn(0.0, 1.0) }
    }

    val pointCount: Int
        get() = value.size

    fun getPoint(index: Int): Double = value[index.coerceIn(0, value.size - 1)]

    fun setPoint(index: Int, y: Double) {
        if (index !in value.indices) return
        val copy = value.copyOf()
        copy[index] = y.coerceIn(0.0, 1.0)
        set(copy)
    }

    fun sample(t: Float): Float {
        if (value.size == 1) return value[0].toFloat()
        val scaled = t.coerceIn(0f, 1f) * (value.size - 1)
        val i = scaled.toInt().coerceIn(0, value.size - 2)
        val frac = scaled - i
        return (value[i] + (value[i + 1] - value[i]) * frac).toFloat()
    }

    override fun toJson(): JsonElement = jsonArray {
        for (v in value) +JsonPrimitive(v)
    }

    override fun fromJsonF(element: JsonElement): DoubleArray? {
        val array = element as? JsonArray ?: return null
        if (array.size() < 2) return null
        return DoubleArray(array.size()) { array[it].asDouble }
    }

    override fun toText(): String = value.joinToString(",")

    override fun fromTextF(text: String): DoubleArray? {
        val parts = text.split(",").map { it.trim() }.takeIf { it.size >= 2 } ?: return null
        val arr = DoubleArray(parts.size)
        for (i in parts.indices) {
            arr[i] = parts[i].toDoubleOrNull() ?: return null
        }
        return arr
    }
}

enum class RangeSlider { LEFT, RIGHT }

enum class FileDialogMode { OPEN_FILE, SAVE_FILE, SELECT_FOLDER }

enum class KeyBindActionMode { HOLD, TOGGLE }
