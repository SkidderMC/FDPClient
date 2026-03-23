/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Color operations and conversions
 *
 * @author Zywl
 */
object RenderColor {

    /**
     * Sets OpenGL color from RGBA components.
     *
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     * @param alpha Alpha component (0-255)
     */
    @JvmStatic
    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    /**
     * Sets OpenGL color from hex color.
     *
     * @param hex ARGB color as integer
     */
    @JvmStatic
    fun glHexColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    /**
     * Sets OpenGL color from Color object.
     *
     * @param color Color object
     */
    @JvmStatic
    fun glColor(color: Color) {
        glColor(color.red, color.green, color.blue, color.alpha)
    }

    /**
     * Sets GlStateManager color from Color object.
     *
     * @param color Color object
     */
    @JvmStatic
    fun glStateManagerColor(color: Color) {
        GlStateManager.color(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )
    }

    /**
     * Sets OpenGL color from hex integer (RGB or ARGB).
     *
     * @param hex Color as integer
     */
    @JvmStatic
    fun glColor(hex: Int) {
        glColor(
            hex shr 16 and 0xFF,
            hex shr 8 and 0xFF,
            hex and 0xFF,
            hex shr 24 and 0xFF
        )
    }

    /**
     * Sets OpenGL color from integer with custom alpha.
     *
     * @param color RGB color
     * @param alpha Alpha value (0.0 to 1.0)
     */
    @JvmStatic
    fun color(color: Int, alpha: Float) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    /**
     * Sets OpenGL color from integer (ARGB).
     *
     * @param color ARGB color
     */
    @JvmStatic
    fun color(color: Int) {
        color(color, (color shr 24 and 0xFF) / 255.0f)
    }

    /**
     * Interpolates between two integer colors.
     *
     * @param color1 First color
     * @param color2 Second color
     * @param amount Interpolation amount (0.0 to 1.0)
     * @return Interpolated color
     */
    @JvmStatic
    fun interpolateColor(color1: Int, color2: Int, amount: Float): Int {
        val amountClamped = amount.coerceIn(0f, 1f)
        val cColor1 = Color(color1, true)
        val cColor2 = Color(color2, true)
        return interpolateColorC(cColor1, cColor2, amountClamped).rgb
    }

    /**
     * Interpolates between two Color objects.
     *
     * @param color1 First color
     * @param color2 Second color
     * @param amount Interpolation amount (0.0 to 1.0)
     * @return Interpolated Color
     */
    @JvmStatic
    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        val amountClamped = amount.coerceIn(0f, 1f)
        return Color(
            interpolateInt(color1.red, color2.red, amountClamped),
            interpolateInt(color1.green, color2.green, amountClamped),
            interpolateInt(color1.blue, color2.blue, amountClamped),
            interpolateInt(color1.alpha, color2.alpha, amountClamped)
        )
    }

    /**
     * Interpolates between two integers.
     *
     * @param oldValue Starting value
     * @param newValue Ending value
     * @param interpolationValue Interpolation amount (0.0 to 1.0)
     * @return Interpolated value
     */
    @JvmStatic
    fun interpolateInt(oldValue: Int, newValue: Int, interpolationValue: Float): Int {
        return interpolateDouble(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toDouble()).toInt()
    }

    /**
     * Interpolates between two double values.
     *
     * @param oldValue Starting value
     * @param newValue Ending value
     * @param interpolationValue Interpolation amount
     * @return Interpolated value
     */
    @JvmStatic
    fun interpolateDouble(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
        return oldValue + (newValue - oldValue) * interpolationValue
    }

    /**
     * Makes a color darker.
     *
     * @param color Original color
     * @param factor Darkening factor (0.0 to 1.0, lower = darker)
     * @return Darker color
     */
    @JvmStatic
    fun darker(color: Color, factor: Float): Color {
        return Color(
            max((color.red * factor).toInt(), 0),
            max((color.green * factor).toInt(), 0),
            max((color.blue * factor).toInt(), 0),
            color.alpha
        )
    }

    /**
     * Makes a color brighter.
     *
     * @param color Original color
     * @param factor Brightening factor (0.0 to 1.0, higher = brighter)
     * @return Brighter color
     */
    @JvmStatic
    fun brighter(color: Color, factor: Float): Color {
        var r = color.red
        var g = color.green
        var b = color.blue
        val alpha = color.alpha

        val i = (1.0 / (1.0 - factor)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i, alpha)
        }

        if (r in 1 until i) r = i
        if (g in 1 until i) g = i
        if (b in 1 until i) b = i

        return Color(
            min((r / factor).toInt(), 255),
            min((g / factor).toInt(), 255),
            min((b / factor).toInt(), 255),
            alpha
        )
    }

    /**
     * Applies opacity to a Color object.
     *
     * @param color Original color
     * @param opacity Opacity value (0.0 to 1.0)
     * @return Color with applied opacity
     */
    @JvmStatic
    fun applyOpacity(color: Color, opacity: Float): Color {
        val opacityClamped = opacity.coerceIn(0f, 1f)
        return Color(color.red, color.green, color.blue, (color.alpha * opacityClamped).toInt())
    }

    /**
     * Applies opacity to an integer color.
     *
     * @param color Original color as integer
     * @param opacity Opacity value (0.0 to 1.0)
     * @return Color with applied opacity
     */
    @JvmStatic
    fun applyOpacity(color: Int, opacity: Float): Int {
        val opacityClamped = opacity.coerceIn(0f, 1f)
        val alpha = ((color shr 24 and 0xFF) * opacityClamped).toInt()
        return (alpha shl 24) or (color and 0x00FFFFFF)
    }

    /**
     * Resets OpenGL color to white.
     */
    @JvmStatic
    fun resetColor() {
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    /**
     * Gets border color based on enchantment level.
     *
     * @param level Enchantment level
     * @return Border color
     */
    @JvmStatic
    fun getBorderColor(level: Int): Int {
        return when {
            level == 2 -> 0x7055FF55
            level == 3 -> 0x7000AAAA
            level == 4 -> 0x70AA0000
            level >= 5 -> 0x70FFAA00
            else -> 0x70FFFFFF
        }
    }
}
