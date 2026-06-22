/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

import kotlin.math.roundToInt

/**
 * Style-independent geometry for the [net.ccbluex.liquidbounce.config.CurveValue] editor.
 *
 * A curve is a list of evenly spaced control points, each holding a height in 0..1. Every style
 * draws the same model: points are spread horizontally across a graph rectangle and each point's
 * height maps to a vertical position (top = 1.0, bottom = 0.0). Dragging a point only changes its
 * height, keeping the curve length stable so it round-trips with the existing serialization.
 */
object CurveEditor {

    /** Screen X of control point [index] (of [count]) inside a graph starting at [gx] with width [gw]. */
    fun pointX(index: Int, count: Int, gx: Float, gw: Float): Float =
        if (count <= 1) gx else gx + gw * index / (count - 1)

    /** Screen Y for a control-point height [value] (0..1) inside a graph at [gy] with height [gh]. */
    fun pointY(value: Double, gy: Float, gh: Float): Float =
        gy + gh * (1f - value.toFloat())

    /** Height (0..1) for a mouse Y inside a graph at [gy] with height [gh]; clamped. */
    fun valueFromY(mouseY: Float, gy: Float, gh: Float): Double {
        if (gh <= 0f) return 0.0
        return (1f - ((mouseY - gy) / gh)).coerceIn(0f, 1f).toDouble()
    }

    /** Index of the control point nearest to [mouseX] inside a graph at [gx] with width [gw]. */
    fun nearestIndex(mouseX: Float, count: Int, gx: Float, gw: Float): Int {
        if (count <= 1) return 0
        if (gw <= 0f) return 0
        val rel = ((mouseX - gx) / gw).coerceIn(0f, 1f)
        return (rel * (count - 1)).roundToInt().coerceIn(0, count - 1)
    }
}
