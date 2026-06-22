/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import kotlin.math.round

/**
 * Float Element - YZY GUI
 * @author opZywl
 */
class FloatElement(
    private val setting: FloatValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : NumericSliderElement(parent, x, y, width, height) {

    override val label: String
        get() = setting.name

    override val minimum: Float
        get() = setting.minimum

    override val maximum: Float
        get() = setting.maximum

    override val currentValue: Float
        get() = setting.get()

    override val suffix: String
        get() = setting.suffix ?: ""

    /** Drag granularity adapts to the range so fractional sliders stay usable. */
    private val step: Float
        get() = when (val span = maximum - minimum) {
            in 0f..5f -> 0.01f
            in 5f..50f -> 0.1f
            else -> if (span <= 0f) 0.01f else 1f
        }

    override fun setValue(value: Float) {
        setting.set(value)
    }

    override fun formatValue(value: Float): String {
        val rounded = round(value * 100f) / 100f
        return if (rounded == rounded.toLong().toFloat()) rounded.toLong().toString() else rounded.toString()
    }

    override fun normalizeDraggedValue(value: Float): Float {
        val s = step
        return if (s > 0f) round(value / s) * s else value
    }
}
