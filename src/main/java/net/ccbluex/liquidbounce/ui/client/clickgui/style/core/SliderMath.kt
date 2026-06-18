/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.Value

/**
 * Pure, style-independent slider arithmetic shared by every ClickGUI style.
 *
 * No GL, fonts, colours or pixel constants live here; the caller supplies the
 * track geometry and receives plain numbers back.
 */
object SliderMath {

    /**
     * Fraction of [value] inside the inclusive [min]..[max] span, coerced to 0..1.
     */
    fun percentOf(value: Double, min: Double, max: Double): Float {
        val span = max - min
        if (span == 0.0) return 0f
        return ((value - min) / span).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Fraction of [mouseX] along a track that starts at [trackX] and is
     * [trackWidth] wide, coerced to 0..1.
     */
    fun percentFromMouse(mouseX: Double, trackX: Double, trackWidth: Double): Float {
        if (trackWidth == 0.0) return 0f
        return ((mouseX - trackX) / trackWidth).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Maps a 0..1 [percent] back onto the [min]..[max] span.
     */
    fun valueFromPercent(percent: Float, min: Double, max: Double): Double =
        percent * (max - min) + min

    /**
     * Returns the (min, max) span for the numeric value types that expose a
     * slider, or null for any other value.
     */
    fun bounds(value: Value<*>): Pair<Double, Double>? = when (value) {
        is IntValue -> value.minimum.toDouble() to value.maximum.toDouble()
        is FloatValue -> value.minimum.toDouble() to value.maximum.toDouble()
        is BlockValue -> value.minimum.toDouble() to value.maximum.toDouble()
        else -> null
    }
}
