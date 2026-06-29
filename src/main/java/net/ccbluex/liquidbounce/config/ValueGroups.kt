/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import net.minecraft.client.gui.FontRenderer
import java.awt.Color

/**
 * Reusable bundles of [Value]s that show and hide together.
 *
 * Both group types are themselves [Configurable]s, so they nest inside a module
 * (or any other [Configurable]) exactly like a normal value and can be registered
 * with the `+` operator. Children added through the group's own helper methods are
 * automatically wired into the existing predicate-visibility mechanism
 * ([Value.setSupport]); when the group is gated off, every child reports
 * `shouldRender() == false` and stops being editable.
 *
 * Example:
 * ```
 * object ExampleModule : Module("Example", Category.MISC) {
 *
 *     // A toggleable bundle: children only show while "Particles" is enabled.
 *     private val particles = ToggleableValueGroup("Particles", enabled = true).apply {
 *         val amount by gatedInt("Amount", 10, 1..50)
 *         val size by gatedFloat("Size", 1f, 0.5f..3f)
 *     }
 *
 *     // A mode bundle: each child is bound to one of the listed modes.
 *     private val color = ModeValueGroup("Color", arrayOf("Static", "Rainbow"), "Static").apply {
 *         val staticColor by gatedColor("StaticColor", Color.WHITE, modes = arrayOf("Static"))
 *         val speed by gatedFloat("RainbowSpeed", 1f, 0.1f..5f, modes = arrayOf("Rainbow"))
 *     }
 *
 *     init {
 *         this += particles
 *         this += color
 *     }
 * }
 * ```
 */

/**
 * A named group whose children are only visible/active while its master
 * [enabledValue] boolean is on.
 */
open class ToggleableValueGroup(
    name: String,
    enabled: Boolean = false,
    private val isSupported: (() -> Boolean)? = null,
) : Configurable(name) {

    val enabledValue = boolean("Enabled", enabled) { isSupported?.invoke() ?: true }

    var enabled by enabledValue

    /**
     * Whether the group is currently expanded: supported and switched on.
     */
    fun isExpanded() = enabledValue.isActive()

    private fun gate(extra: (() -> Boolean)?): () -> Boolean = {
        enabled && (isSupported?.invoke() ?: true) && (extra?.invoke() ?: true)
    }

    fun gatedInt(name: String, value: Int, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.int(name, value, range, suffix, gate(isSupported))

    fun gatedFloat(name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.float(name, value, range, suffix, gate(isSupported))

    fun gatedBoolean(name: String, value: Boolean, isSupported: (() -> Boolean)? = null) =
        super.boolean(name, value, gate(isSupported))

    fun gatedChoices(name: String, values: Array<String>, value: String, isSupported: (() -> Boolean)? = null) =
        super.choices(name, values, value, gate(isSupported))

    fun gatedText(name: String, value: String, isSupported: (() -> Boolean)? = null) =
        super.text(name, value, gate(isSupported))

    fun gatedBlock(name: String, value: Int, isSupported: (() -> Boolean)? = null) =
        super.block(name, value, gate(isSupported))

    fun gatedFont(name: String, value: FontRenderer, isSupported: (() -> Boolean)? = null) =
        super.font(name, value, gate(isSupported))

    fun gatedIntRange(name: String, value: IntRange, range: IntRange, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.intRange(name, value, range, suffix, gate(isSupported))

    fun gatedFloatRange(name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, isSupported: (() -> Boolean)? = null) =
        super.floatRange(name, value, range, suffix, gate(isSupported))

    fun gatedColor(name: String, value: Color, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(isSupported))

    fun gatedColor(name: String, value: Int, rainbow: Boolean = false, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(isSupported))
}

/**
 * A named group whose children are gated by which mode of [modeValue] is
 * selected. Each child declares the [modes] it belongs to and is only
 * visible/active while one of those modes is chosen.
 */
open class ModeValueGroup(
    name: String,
    modes: Array<String>,
    defaultMode: String = modes.first(),
    private val isSupported: (() -> Boolean)? = null,
) : Configurable(name) {

    private val modeChoices = linkedMapOf<String, ModeChoice>()
    private var activeChoice: ModeChoice? = null

    val modeValue = choices("Mode", modes, defaultMode) { isSupported?.invoke() ?: true }.apply {
        onChanged(::activateChoice)
    }

    var mode by modeValue

    /**
     * Whether [candidate] is the currently selected mode (and the group is supported).
     */
    fun isMode(candidate: String) =
        modeValue.get().equals(candidate, true) && (isSupported?.invoke() ?: true)

    private fun gate(modes: Array<String>, extra: (() -> Boolean)?): () -> Boolean = {
        modes.any { isMode(it) } && (extra?.invoke() ?: true)
    }

    /** Registers a mode-owned configurable with selection lifecycle callbacks. */
    fun mode(
        name: String,
        onSelected: () -> Unit = {},
        onDeselected: () -> Unit = {},
        configure: ModeChoice.() -> Unit = {},
    ): ModeChoice {
        require(modeValue.values.any { it.equals(name, true) }) { "Unknown mode $name in ${this.name}" }
        val choice = ModeChoice(name, onSelected, onDeselected).apply {
            setSupport { isMode(name) }
            configure()
        }
        modeChoices[name.lowercase()] = choice
        addValue(choice)
        if (isMode(name)) activateChoice(modeValue.get())
        return choice
    }

    private fun activateChoice(selected: String) {
        val next = modeChoices[selected.lowercase()]
        if (next === activeChoice) return
        activeChoice?.deselect()
        activeChoice = next
        next?.select()
    }

    fun gatedInt(name: String, value: Int, range: IntRange, suffix: String? = null, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.int(name, value, range, suffix, gate(modes, isSupported))

    fun gatedFloat(name: String, value: Float, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.float(name, value, range, suffix, gate(modes, isSupported))

    fun gatedBoolean(name: String, value: Boolean, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.boolean(name, value, gate(modes, isSupported))

    fun gatedChoices(name: String, values: Array<String>, value: String, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.choices(name, values, value, gate(modes, isSupported))

    fun gatedText(name: String, value: String, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.text(name, value, gate(modes, isSupported))

    fun gatedBlock(name: String, value: Int, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.block(name, value, gate(modes, isSupported))

    fun gatedFont(name: String, value: FontRenderer, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.font(name, value, gate(modes, isSupported))

    fun gatedIntRange(name: String, value: IntRange, range: IntRange, suffix: String? = null, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.intRange(name, value, range, suffix, gate(modes, isSupported))

    fun gatedFloatRange(name: String, value: ClosedFloatingPointRange<Float>, range: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE, suffix: String? = null, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.floatRange(name, value, range, suffix, gate(modes, isSupported))

    fun gatedColor(name: String, value: Color, rainbow: Boolean = false, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(modes, isSupported))

    fun gatedColor(name: String, value: Int, rainbow: Boolean = false, modes: Array<String>, isSupported: (() -> Boolean)? = null) =
        super.color(name, value, rainbow, gate(modes, isSupported))
}

class ModeChoice(
    name: String,
    private val onSelected: () -> Unit,
    private val onDeselected: () -> Unit,
) : Configurable(name) {
    var selected: Boolean = false
        private set

    internal fun select() {
        if (selected) return
        selected = true
        onSelected()
    }

    internal fun deselect() {
        if (!selected) return
        selected = false
        onDeselected()
    }
}
