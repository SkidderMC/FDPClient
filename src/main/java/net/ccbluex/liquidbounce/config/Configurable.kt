/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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

    var groupExpanded: Boolean = true

    fun addValue(value: Value<*>) = apply {
        value.owner?.takeIf { it !== this }?.removeValue(value)
        val list = get()
        if (value !in list) list.add(value)
        value.owner = this
    }

    fun addValues(values: Collection<Value<*>>) = apply {
        values.toList().forEach(::addValue)
    }

    /**
     * Exposes a value owned by another container (e.g. a shared manager singleton) in this
     * container too, without stealing ownership: every sharing container renders and
     * serializes it, while the canonical owner keeps it.
     */
    fun shareValue(value: Value<*>) = apply {
        val list = get()
        if (value !in list) list.add(value)
    }

    fun <T, V : Value<T>> shared(value: V): V {
        shareValue(value)
        return value
    }

    fun removeValue(value: Value<*>): Boolean {
        val removed = get().remove(value)
        if (removed && value.owner === this) value.owner = null
        return removed
    }

    fun moveValueTo(value: Value<*>, target: Configurable): Boolean {
        if (value.owner !== this || !removeValue(value)) return false
        target.addValue(value)
        return true
    }

    fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) values.filter { it.matchesKey(name) }.forEach { value ->
            if (value.owner === this) {
                group.addValue(value)
            } else {
                // Shared value (owned by another container): relocate this container's view of it
                // into the group without stealing it from the canonical owner.
                get().remove(value)
                group.shareValue(value)
            }
        }
    }

    fun group(name: String, vararg names: String): Configurable =
        Configurable(name).also { moveValues(it, *names); addValue(it) }

    fun group(parent: Configurable, name: String, vararg names: String): Configurable =
        Configurable(name).also { moveValues(it, *names); parent.addValue(it) }

    fun findDeep(valueName: String): Value<*>? {
        // Prefer editable leaves over containers. A configurable is allowed to have the same
        // name as one of its children (for example the SmartAutoBlock group and its toggle).
        // Returning the group first makes the leaf impossible to address through module
        // commands and other name-based integrations.
        for (value in values) {
            if (value !is Configurable && value.matchesKey(valueName)) return value
        }

        for (value in values.filterIsInstance<Configurable>()) {
            value.findDeep(valueName)?.let { return it }
        }

        for (value in values) {
            if (value is Configurable && value.matchesKey(valueName)) return value
        }

        return null
    }

    operator fun <T, V : Value<T>> V.unaryPlus() = apply(::addValue)

    override fun toJson(): JsonElement = toJson(IncludeConfiguration.LOCAL)

    fun toJson(include: IncludeConfiguration): JsonElement = json {
        for (value in values) {
            if (value.excluded ||
                (!include.subjectiveValues && value.subjective) ||
                (!include.hiddenValues && value.hidden) ||
                (!include.keyBindings && value is KeyBindValue) ||
                (!include.filePaths && value is FileValue)
            ) {
                continue
            }

            value.name to if (value is Configurable) value.toJson(include) else value.toJson()
        }
    }

    override fun fromJsonF(element: JsonElement): MutableList<Value<*>>? {
        element as JsonObject

        val values = get()
        // Set all sub values from the JSON object
        for ((valueName, value) in element.entrySet()) {
            values.find { it.matchesKey(valueName) }?.fromJson(value)
        }

        return values
    }

    override fun toText(): String = toJson().toString()

    override fun fromTextF(text: String): MutableList<Value<*>>? {
        val element = runCatching { JsonParser().parse(text) }.getOrNull() as? JsonObject ?: return null
        return fromJsonF(element)
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

    fun double(
        name: String, value: Double, range: ClosedFloatingPointRange<Double>, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +DoubleValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun long(
        name: String, value: Long, range: LongRange, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +LongValue(name, value, range, suffix).apply {
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

    fun fileValue(
        name: String, value: String = "", dialogMode: FileDialogMode = FileDialogMode.OPEN_FILE,
        extensions: List<String> = emptyList(), isSupported: (() -> Boolean)? = null
    ) = +FileValue(name, value, dialogMode, extensions).apply {
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

    fun doubleRange(
        name: String, value: ClosedFloatingPointRange<Double>, range: ClosedFloatingPointRange<Double>,
        suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +DoubleRangeValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun longRange(
        name: String, value: LongRange, range: LongRange, suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +LongRangeValue(name, value, range, suffix).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun refreshableRange(
        name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
        suffix: String? = null, isSupported: (() -> Boolean)? = null
    ) = +RefreshableRangeValue(name, value, range, suffix).apply {
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

    fun multiSelect(
        name: String, choices: Array<String>, default: Set<String> = emptySet(), isSupported: (() -> Boolean)? = null
    ) = +MultiSelectValue(name, default, choices).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun <T : Any> registryMultiSelect(
        name: String, registry: Map<String, T>, default: Set<String> = emptySet(),
        isSupported: (() -> Boolean)? = null,
    ) = +RegistryMultiSelectValue(name, default, registry).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun mutableList(
        name: String, value: List<String> = emptyList(), isSupported: (() -> Boolean)? = null
    ) = +MutableListValue(name, value).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun keybind(
        name: String, default: Int = org.lwjgl.input.Keyboard.KEY_NONE,
        action: KeyBindActionMode = KeyBindActionMode.TOGGLE, isSupported: (() -> Boolean)? = null
    ) = +KeyBindValue(name, default, action).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun vec3(
        name: String, x: Double, y: Double, z: Double, useLocateButton: Boolean = false,
        isSupported: (() -> Boolean)? = null
    ) = +Vec3Value(name, doubleArrayOf(x, y, z), useLocateButton).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun vec2(
        name: String, x: Double, y: Double, useLocateButton: Boolean = false,
        isSupported: (() -> Boolean)? = null
    ) = +Vec2Value(name, doubleArrayOf(x, y), useLocateButton).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }

    fun curveValue(
        name: String, points: DoubleArray = doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0), isSupported: (() -> Boolean)? = null
    ) = +CurveValue(name, points).apply {
        if (isSupported != null) setSupport { isSupported.invoke() }
    }
}
