/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.*

object ColorUtils {
    /** Array of the special characters that are allowed in any text drawing of Minecraft.  */
    val allowedCharactersArray = charArrayOf('/', '\n', '\r', '\t', '\u0000', '', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

    fun isAllowedCharacter(character: Char) =
        character.code != 167 && character.code >= 32 && character.code != 127

    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    val hexColors = IntArray(16)

    init {
        repeat(16) { i ->
            val baseColor = (i shr 3 and 1) * 85

            val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
            val green = (i shr 1 and 1) * 170 + baseColor
            val blue = (i and 1) * 170 + baseColor

            hexColors[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }

    fun stripColor(input: String): String = COLOR_PATTERN.matcher(input).replaceAll("")

    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.lastIndex) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1], true)) {
                chars[i] = '§'
                chars[i + 1] = Character.toLowerCase(chars[i + 1])
            }
        }

        return String(chars)
    }

    fun randomMagicText(text: String): String {
        val stringBuilder = StringBuilder()
        val allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"

        for (c in text.toCharArray()) {
            if (isAllowedCharacter(c)) {
                val index = nextInt(endExclusive = allowedCharacters.length)
                stringBuilder.append(allowedCharacters.toCharArray()[index])
            }
        }

        return stringBuilder.toString()
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

    fun rainbow(offset: Long = 400000L, alpha: Float = 1f): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, alpha)
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

    fun getGradientOffset(color1: Color, color2: Color, offset: Double): Color {
        var offset = offset
        if (offset > 1) {
            val left = offset % 1
            val off = offset.toInt()
            offset = if (off % 2 == 0) left else 1 - left
        }
        val percent = 1 - offset
        val red = (color1.red * percent + color2.red * offset).toInt()
        val green = (color1.green * percent + color2.green * offset).toInt()
        val part = (color1.blue * percent + color2.blue * offset).toInt()
        return Color(red, green, part)
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

    fun fade(speed: Int, index: Int, color: Color, alpha: Float): Color {
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

    fun skyRainbow(var2: Int, st: Float, bright: Float, speed: Float): Color {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109 * speed).toLong()).toDouble()) / 5
        return Color.getHSBColor(
            if ((((360.0.also { v1 %= it }) / 360.0).toFloat()) < 0.5) -((v1 / 360.0).toFloat()) else (v1 / 360.0).toFloat(),
            st,
            bright
        )
    }

    fun getHealthColor(health: Float, maxHealth: Float): Color {
        val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
        val colors = arrayOf(Color(108, 0, 0), Color(255, 51, 0), Color.GREEN)
        val progress = health / maxHealth
        return blendColors(fractions, colors, progress).brighter()
    }

    fun blendColors(fractions: FloatArray, colors: Array<Color>, progress: Float): Color {
        if (fractions.size == colors.size) {
            val indices: IntArray =
               getFractionIndices(fractions, progress)
            val range = floatArrayOf(fractions[indices[0]], fractions[indices[1]])
            val colorRange = arrayOf(colors[indices[0]], colors[indices[1]])
            val max = range[1] - range[0]
            val value = progress - range[0]
            val weight = value / max
            return blend(
                colorRange[0],
                colorRange[1],
                (1.0f - weight).toDouble()
            )
        } else {
            throw IllegalArgumentException("Fractions and colours must have equal number of elements")
        }
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

}
