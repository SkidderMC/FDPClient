/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.attack.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.*

object ColorUtils {
    /** Array of the special characters that are allowed in any text drawing of Minecraft.  */
    val allowedCharactersArray =
        charArrayOf('/', '\n', '\r', '\t', '\u0000', '', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

    fun isAllowedCharacter(character: Char) =
        character.code != 167 && character.code >= 32 && character.code != 127

    fun isValidColorInput(input: String): Boolean {
        val regex = Regex("^(0|[1-9][0-9]{0,2})$")
        return regex.matches(input)
    }

    val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    val hexColors = IntArray(16) { i ->
        val baseColor = (i shr 3 and 1) * 85

        val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
        val green = (i shr 1 and 1) * 170 + baseColor
        val blue = (i and 1) * 170 + baseColor

        (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255)
    }

    val minecraftRed = Color(255, 85, 85) // §c

    fun Color.withAlpha(a: Int) = Color(red, green, blue, a)
    fun Color.normalize() = Color(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)

    fun packARGBValue(r: Int, g: Int, b: Int, a: Int = 0xff): Int {
        return (a and 255 shl 24) or (r and 255 shl 16) or (g and 255 shl 8) or (b and 255)
    }
    fun unpackARGBValue(argb: Int): IntArray {
        return intArrayOf(
            argb ushr 24 and 0xFF,
            argb ushr 16 and 0xFF,
            argb ushr 8 and 0xFF,
            argb and 0xFF
        )
    }

    fun hexToColorInt(str: String): Int {
        val hex = str.removePrefix("#")

        if (hex.isEmpty()) Color.WHITE.rgb

        val expandedHex = when (hex.length) {
            1 -> hex.repeat(3) + "FF"
            2 -> hex.repeat(3) + "FF"
            3 -> hex[0].toString().repeat(2) + hex[1].toString().repeat(2) + hex[2].toString().repeat(2) + "FF"
            6 -> hex + "FF"
            8 -> hex
            else -> throw IllegalArgumentException("Invalid hex color format")
        }

        return Color.decode("#$expandedHex").rgb
    }

    fun unpackARGBFloatValue(argb: Int): FloatArray {
        return floatArrayOf(
            (argb ushr 24 and 0xFF) / 255F,
            (argb ushr 16 and 0xFF) / 255F,
            (argb ushr 8 and 0xFF) / 255F,
            (argb and 0xFF) / 255F
        )
    }

    fun stripColor(input: String): String = COLOR_PATTERN.matcher(input).replaceAll("")

    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.lastIndex) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1])) {
                chars[i] = '§'
                chars[i + 1] = chars[i + 1].lowercaseChar()
            }
        }

        return String(chars)
    }

    private val allowedCharArray =
        "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"
            .toCharArray()

    fun randomMagicText(text: String): String = buildString(text.length) {
        for (c in text) {
            if (isAllowedCharacter(c)) {
                val index = nextInt(endExclusive = allowedCharArray.size)
                append(allowedCharArray[index])
            }
        }
    }

    fun rainbow(): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + 400000L) / 10000000000F % 1, 1F, 1F))
        return Color(
            currentColor.red / 255F * 1F,
            currentColor.green / 255f * 1F,
            currentColor.blue / 255F * 1F,
            currentColor.alpha / 255F
        )
    }

    fun rainbow(offset: Long): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(
            currentColor.red / 255F * 1F, currentColor.green / 255F * 1F, currentColor.blue / 255F * 1F,
            currentColor.alpha / 255F
        )
    }

    fun rainbow(alpha: Float) = rainbow(400000L, alpha)

    fun rainbow(alpha: Int) = rainbow(400000L, alpha / 255)

    fun rainbow(offset: Long, alpha: Int) = rainbow(offset, alpha.toFloat() / 255)

    fun blendColors(color: Color, color2: Color): Color {
        val alpha = color2.alpha / 255.0
        val red = (color2.red * alpha + color.red * (1 - alpha)).toInt()
        val green = (color2.green * alpha + color.green * (1 - alpha)).toInt()
        val blue = (color2.blue * alpha + color.blue * (1 - alpha)).toInt()
        return Color(red, green, blue)
    }

    fun rainbow(offset: Long = 400000L, alpha: Float = 1f): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F, currentColor.green / 255F, currentColor.blue / 255F, alpha)
    }

    fun interpolateColor(start: Color, end: Color, ratio: Float): Color {
        val t = ratio.coerceIn(0.0f, 1.0f)

        val r = (start.red + (end.red - start.red) * t).toInt()
        val g = (start.green + (end.green - start.green) * t).toInt()
        val b = (start.blue + (end.blue - start.blue) * t).toInt()
        val a = (start.alpha + (end.alpha - start.alpha) * t).toInt()

        return Color(r, g, b, a)
    }

    fun interpolateHSB(startColor: Color, endColor: Color, process: Float): Color {
        val startHSB = Color.RGBtoHSB(startColor.red, startColor.green, startColor.blue, null)
        val endHSB = Color.RGBtoHSB(endColor.red, endColor.green, endColor.blue, null)

        val brightness = (startHSB[2] + endHSB[2]) / 2
        val saturation = (startHSB[1] + endHSB[1]) / 2

        val hueMax = if (startHSB[0] > endHSB[0]) startHSB[0] else endHSB[0]
        val hueMin = if (startHSB[0] > endHSB[0]) endHSB[0] else startHSB[0]

        val hue = (hueMax - hueMin) * process + hueMin
        return Color.getHSBColor(hue, saturation, brightness)
    }

    fun getGradientOffset(c1: Color, c2: Color, offsetIn: Double): Color {
        var offset = offsetIn
        if (offset > 1.0) {
            val left = offset % 1.0
            val off = offset.toInt()
            offset = if (off % 2 == 0) left else 1.0 - left
        }
        val inv = 1.0 - offset

        fun mix(a: Int, b: Int) =
            (a * inv + b * offset).toInt().coerceIn(0, 255)

        val r = mix(c1.red,   c2.red)
        val g = mix(c1.green, c2.green)
        val b = mix(c1.blue,  c2.blue)
        val a = mix(c1.alpha, c2.alpha)

        return Color(r, g, b, a)
    }

    fun shiftHue(color: Color, shift: Int): Color {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        val shiftedColor = Color(Color.HSBtoRGB((hsb[0] + shift.toFloat() / 360) % 1F, hsb[1], hsb[2]))
        return Color(shiftedColor.red, shiftedColor.green, shiftedColor.blue, color.alpha)
    }

    fun fade(colorSettings: ColorSettingsInteger, speed: Int, count: Int): Color {
        val color = colorSettings.color()

        return fade(color, speed, count)
    }

    fun fade(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness =
            abs(((System.currentTimeMillis() % 2000L).toFloat() / 1000.0f + index.toFloat() / count.toFloat() * 2.0f) % 2.0f - 1.0f)
        brightness = 0.5f + 0.5f * brightness
        hsb[2] = brightness % 2.0f
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }


    fun fade(colorSettings: ColorSettingsInteger, speed: Int, index: Int, color: Color, alpha: Float): Color {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        var angle = ((System.currentTimeMillis() / speed + index) % 360L).toInt()
        angle = (if (angle > 180) 360 - angle else angle) + 180
        val colorHSB = Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0f))
        return Color(colorHSB.red, colorHSB.green, colorHSB.blue, (max(0.0, min(255.0, (alpha * 255.0f).toDouble()))).toInt())
    }

    fun setColor(color: Int) {
        setColorAlpha(color)
    }

    private fun setColorAlpha(color: Int) {
        val alpha = (color shr 24 and 255) / 255f
        val red = (color shr 16 and 255) / 255f
        val green = (color shr 8 and 255) / 255f
        val blue = (color and 255) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun clearColor() {
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun getCustomColor(red: Int, green: Int, blue: Int, alpha: Int): Int {
        var color = 0
        color = color or (alpha shl 24)
        color = color or (red shl 16)
        color = color or (green shl 8)
        return blue.let { color = color or it; color }
    }

    fun swapAlpha(color: Int, alpha: Float): Int {
        val f = color shr 16 and 0xFF
        val f1 = color shr 8 and 0xFF
        val f2 = color and 0xFF
        return getCustomColor(f, f1, f2, alpha.toInt())
    }

    fun reAlpha(color: Int, alpha: Float): Color {
        val c = Color(color)
        val r = 0.003921569f * c.red.toFloat()
        val g = 0.003921569f * c.green.toFloat()
        val b = 0.003921569f * c.blue.toFloat()
        return (Color(r, g, b, alpha))
    }

    fun targetReAlpha(color: Color, alpha: Float): Color {
        return Color(color.red / 255f, color.green / 255f, color.blue / 255f, alpha)
    }

    @JvmStatic
    fun inAlpha(color: Color, alpha: Int): Color = Color(color.red, color.green, color.blue, alpha.coerceIn(0, 255))

    @JvmStatic
    fun getOppositeColor(color: Color): Color = Color(255 - color.red, 255 - color.green, 255 - color.blue, color.alpha)

    fun rainbowEffect(offset: Long, saturation: Float, fade: Float): Color {
        val hue = (System.nanoTime() + offset).toFloat() / 1.0E10f % 1.0f
        val color = Integer.toHexString(Color.HSBtoRGB(hue, saturation, 1.0f)).toLong(16)
        val c = Color(color.toInt())
        return Color(c.red / 255.0f * fade, c.green / 255.0f * fade, c.blue / 255.0f * fade, c.alpha / 255.0f)
    }

    fun mixColors(color1: Color, color2: Color, ms: Double, offset: Int): Color {
        val timer = (System.currentTimeMillis() / 1E+8 * ms) * 4E+5
        val percent =  (sin(timer + offset * 0.55f) + 1) * 0.5f
        val inverse_percent = 1.0 - percent
        val redPart = (color1.red * percent + color2.red * inverse_percent).toInt()
        val greenPart = (color1.green * percent + color2.green * inverse_percent).toInt()
        val bluePart = (color1.blue * percent + color2.blue * inverse_percent).toInt()
        return Color(redPart, greenPart, bluePart)
    }

    fun mixColors(color1: Color, color2: Color, percent: Float): Color {
        return Color(color1.red + ((color2.red - color1.red) * percent).toInt(), color1.green + ((color2.green - color1.green) * percent).toInt(), color1.blue + ((color2.blue - color1.blue) * percent).toInt(), color1.alpha + ((color2.alpha - color1.alpha) * percent).toInt())
    }

    fun skyRainbow(var2: Int, st: Float, bright: Float, speed: Float): Color {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109 * speed).toLong()).toDouble()) / 5
        return Color.getHSBColor(
            if ((((360.0.also { v1 %= it }) / 360.0).toFloat()) < 0.5) -((v1 / 360.0).toFloat()) else (v1 / 360.0).toFloat(),
            st,
            bright
        )
    }

    fun getHealthColor(health: Float?, maxHealth: Float?): Color {
        val safeHealth = health ?: 0.0f
        val safeMaxHealth = maxHealth ?: 1.0f
        val progress = (safeHealth / safeMaxHealth).coerceIn(0.0f, 1.0f)
        val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
        val colors = arrayOf(Color(108, 0, 0), Color(255, 51, 0), Color.GREEN)
        return blendColors(fractions, colors, progress).brighter()
    }

    fun blendColors(fractions: FloatArray, colors: Array<Color>, progress: Float): Color {
        if (fractions.size != colors.size) {
            throw IllegalArgumentException("Fractions and colors must have equal number of elements")
        }

        val indices = getFractionIndices(fractions, progress)
        val range = floatArrayOf(fractions[indices[0]], fractions[indices[1]])
        val colorRange = arrayOf(colors[indices[0]], colors[indices[1]])

        val max = range[1] - range[0]
        val value = progress - range[0]
        val weight = value / max

        return blend(colorRange[0], colorRange[1], (1.0f - weight).toDouble())
    }

    fun getFractionIndices(fractions: FloatArray, progress: Float): IntArray {
        val range = IntArray(2)

        var startPoint: Int
        startPoint = 0
        while (startPoint < fractions.size && fractions[startPoint] <= progress) {
            ++startPoint
        }

        if (startPoint >= fractions.size) {
            startPoint = fractions.size - 1
        }

        range[0] = startPoint - 1
        range[1] = startPoint
        return range
    }

    fun applyOpacity(color: Int, opacity: Float): Int {
        val old = Color(color)
        return applyOpacity(old, opacity).rgb
    }

    fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1.0, max(0.0, opacity.toDouble())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    fun darker(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 0xFF) * factor).toInt()
        val g = ((color shr 8 and 0xFF) * factor).toInt()
        val b = ((color and 0xFF) * factor).toInt()
        val a = color shr 24 and 0xFF
        return (r and 0xFF) shl 16 or ((g and 0xFF) shl 8) or (b and 0xFF) or ((a and 0xFF) shl 24)
    }

    fun getAlphaFromColor(color: Int): Int {
        return color shr 24 and 0xFF
    }

    fun glFloatColor(color: Color, alpha: Int) {
        glFloatColor(color, alpha / 255f)
    }

    fun glFloatColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f

        color(red, green, blue, alpha)
    }

    fun getMainColor(level: Int): Int {
        if (level == 4) return -0x560000
        return -1
    }

    fun interpolateColor(color1: Int, color2: Int, amount: Float): Int {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        val cColor1 = Color(color1)
        val cColor2 = Color(color2)
        return interpolateColorC(cColor1, cColor2, amount).getRGB()
    }

    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        return Color(
            interpolateInt(color1.red, color2.red, amount.toDouble()),
            interpolateInt(color1.green, color2.green, amount.toDouble()),
            interpolateInt(color1.blue, color2.blue, amount.toDouble()),
            interpolateInt(color1.alpha, color2.alpha, amount.toDouble())
        )
    }

    fun interpolateInt(oldValue: Int, newValue: Int, interpolationValue: Double): Int {
        return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble())
            .toInt()
    }

    fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
        return (oldValue + (newValue - oldValue) * interpolationValue)
    }

    fun blend(color1: Color, color2: Color, ratio: Double): Color {
        val r = ratio.toFloat()
        val ir = 1.0f - r
        val rgb1 = color1.getColorComponents(FloatArray(3))
        val rgb2 = color2.getColorComponents(FloatArray(3))
        var red = rgb1[0] * r + rgb2[0] * ir
        var green = rgb1[1] * r + rgb2[1] * ir
        var blue = rgb1[2] * r + rgb2[2] * ir
        if (red < 0.0f) {
            red = 0.0f
        } else if (red > 255.0f) {
            red = 255.0f
        }

        if (green < 0.0f) {
            green = 0.0f
        } else if (green > 255.0f) {
            green = 255.0f
        }

        if (blue < 0.0f) {
            blue = 0.0f
        } else if (blue > 255.0f) {
            blue = 255.0f
        }

        var color3: Color? = null

        try {
            color3 = Color(red, green, blue)
        } catch (ignored: java.lang.IllegalArgumentException) {
        }

        return color3!!
    }

    fun setColour(colour: Int) {
        val a = (colour shr 24 and 0xFF) / 255.0f
        val r = (colour shr 16 and 0xFF) / 255.0f
        val g = (colour shr 8 and 0xFF) / 255.0f
        val b = (colour and 0xFF) / 255.0f
        GL11.glColor4f(r, g, b, a)
    }

    enum class potionColor(val c: Int) {
        WHITE(-65794),
        GREY(-9868951);

        companion object {
            fun getColor(brightness: Int): Int {
                return getColor(brightness, brightness, brightness, 255)
            }

            fun getColor(brightness: Int, alpha: Int): Int {
                return getColor(brightness, brightness, brightness, alpha)
            }

            fun getColor(red: Int, green: Int, blue: Int): Int {
                return getColor(red, green, blue, 255)
            }

            fun getColor(red: Int, green: Int, blue: Int, alpha: Int): Int {
                var color = 0
                color = color or (alpha shl 24)
                color = color or (red shl 16)
                color = color or (green shl 8)
                color = color or blue
                return color
            }
        }
    }

    fun interpolateHealthColor(
        entity: EntityLivingBase,
        r: Int,
        g: Int,
        b: Int,
        a: Int,
        healthFromScoreboard: Boolean,
        absorption: Boolean
    ): Color {
        val entityHealth = getHealth(entity, healthFromScoreboard, absorption)
        val healthRatio = (entityHealth / entity.maxHealth).coerceIn(0F, 1F)
        val red = (r * (1 - healthRatio)).toInt()
        val green = (g * healthRatio).toInt()
        return Color(red, green, b, a)
    }
}