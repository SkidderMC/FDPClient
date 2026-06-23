/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import java.awt.Color
import kotlin.math.roundToInt

/** Four immutable 8-bit color channels stored in ARGB order inside one integer. */
@JvmInline
value class Color4b(val packed: Int) {
    val alpha: Int get() = packed ushr 24 and 0xFF
    val red: Int get() = packed ushr 16 and 0xFF
    val green: Int get() = packed ushr 8 and 0xFF
    val blue: Int get() = packed and 0xFF

    fun withAlpha(alpha: Int): Color4b = fromArgb(alpha, red, green, blue)

    fun toAwtColor(): Color = Color(red, green, blue, alpha)

    fun interpolate(other: Color4b, amount: Float): Color4b {
        val ratio = amount.coerceIn(0f, 1f)
        fun channel(start: Int, end: Int) = (start + (end - start) * ratio).roundToInt()
        return fromArgb(
            channel(alpha, other.alpha),
            channel(red, other.red),
            channel(green, other.green),
            channel(blue, other.blue)
        )
    }

    companion object {
        @JvmStatic
        fun fromArgb(alpha: Int, red: Int, green: Int, blue: Int): Color4b {
            require(alpha in 0..255 && red in 0..255 && green in 0..255 && blue in 0..255) {
                "Color channels must be between 0 and 255"
            }
            return Color4b(alpha shl 24 or (red shl 16) or (green shl 8) or blue)
        }

        @JvmStatic
        fun fromRgba(red: Int, green: Int, blue: Int, alpha: Int = 255): Color4b =
            fromArgb(alpha, red, green, blue)

        @JvmStatic
        fun fromAwtColor(color: Color): Color4b = fromRgba(color.red, color.green, color.blue, color.alpha)
    }
}
