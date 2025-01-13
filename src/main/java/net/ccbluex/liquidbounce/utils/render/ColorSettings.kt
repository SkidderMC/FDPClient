/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.config.color
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import java.awt.Color

class ColorSettingsFloat(owner: Any, name: String, val index: Int? = null, generalApply: () -> Boolean = { true }) {
    private val colors = color(
        "$name${index ?: "Color"}",
        Color(
            if ((index ?: 0) % 3 == 1) 255 else 0,
            if ((index ?: 0) % 3 == 2) 255 else 0,
            if ((index ?: 0) % 3 == 0) 255 else 0
        ), subjective = true
    ) { generalApply() }

    fun color() = colors.selectedColor()

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int = MAX_GRADIENT_COLORS, generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsFloat> {
            return (1..colors).map { ColorSettingsFloat(owner, name, it) { generalApply(it) } }
        }
    }
}

class ColorSettingsInteger(
    owner: Any, name: String? = null,
    val index: Int? = null,
    applyMax: Boolean = false,
    generalApply: () -> Boolean = { true }
) {
    private val string = if (name == null) "Color" else "$name"
    private val max = if (applyMax) 255 else 0

    private val colors = color("${string}${index ?: ""}", Color(max, max, max, 255), subjective = true)
    { generalApply() }

    fun color(a: Int = colors.selectedColor().alpha) = color().withAlpha(a)

    fun color() = colors.selectedColor()

    fun with(
        r: Int = color().red, g: Int = color().green, b: Int = color().blue, a: Int = color().alpha
    ): ColorSettingsInteger {
        Color(r, g, b, a).let {
            colors.setAndUpdateDefault(it)
            colors.initializeSliderValues(it)
        }
        return this
    }

    fun with(color: Color) = with(color.red, color.green, color.blue, color.alpha)

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int, applyMax: Boolean = false, generalApply: (Int) -> Boolean = { true }
        ): List<ColorSettingsInteger> {
            return (1..colors).map {
                ColorSettingsInteger(owner, name, it, applyMax) { generalApply(it) }
            }
        }
    }
}

fun List<ColorSettingsFloat>.toColorArray(max: Int) = (0 until max).map {
    val colors = this[it].color()

    floatArrayOf(
        colors.red.toFloat() / 255f,
        colors.green.toFloat() / 255f,
        colors.blue.toFloat() / 255f,
        1f
    )
}