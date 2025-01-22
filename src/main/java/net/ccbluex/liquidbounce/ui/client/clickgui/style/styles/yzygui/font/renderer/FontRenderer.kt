/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.CustomFont
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max

/**
 * @author opZywl - Font Renderer
 */
class FontRenderer(resourceLocation: ResourceLocation?, size: Float) : CustomFont(resourceLocation, size) {
    private val boldItalicChars = Array(256) { CharData() }
    private val italicChars = Array(256) { CharData() }
    private val boldChars = Array(256) { CharData() }

    private val colorCode = IntArray(32)
    private val COLOR_CODE_START = '§'
    private val charWidthFloat = FloatArray(256)
    private val glyphWidth = ByteArray(65536)
    private var texBold: DynamicTexture? = null
    private var texItalic: DynamicTexture? = null
    private var texItalicBold: DynamicTexture? = null
    private val unicodeFlag = false

    /**
     * @param resourceLocation A resource location for the font.
     * @param size The font size
     */
    init {
        setupMinecraftColorCodes()
        setupBoldItalicIDs()
    }

    fun drawStringWithShadow(text: String?, x: Double, y: Double, color: Int, shadowColor: Int): Float {
        val shadowWidth = drawString(text, x + 0.5, y + 0.5, shadowColor, false)

        return max(shadowWidth.toDouble(), drawString(text, x, y, color, false).toDouble()).toFloat()
    }

    fun drawStringWithShadow(text: String?, x: Double, y: Double, color: Color, shadowColor: Color): Float {
        val shadowWidth = drawString(text, x + 0.5, y + 0.5, shadowColor.rgb, false)

        return max(shadowWidth.toDouble(), drawString(text, x, y, color.rgb, false).toDouble()).toFloat()
    }

    fun drawStringWithShadow(text: String?, x: Double, y: Double, color: Int): Float {
        val shadowWidth = drawString(text, x + 0.5, y + 0.5, color, true)

        return max(shadowWidth.toDouble(), drawString(text, x, y, color, false).toDouble()).toFloat()
    }

    fun drawStringWithShadow(text: String?, x: Double, y: Double, color: Color): Float {
        return drawStringWithShadow(text, x, y, color.rgb)
    }

    fun drawString(text: String?, x: Float, y: Float, color: Int): Float {
        return drawString(text, x.toDouble(), y.toDouble(), color, false)
    }

    fun drawCenteredString(text: String?, x: Float, y: Float, color: Int): Float {
        return drawString(text, x - getWidth(text).toFloat() / 2, y - height / 2, color)
    }

    fun drawCenteredStringWithShadow(text: String?, x: Float, y: Float, color: Int): Float {
        drawString(text, x - getWidth(text).toDouble() / 2 + 0.55, y - height / 2 + 0.55, color, true)
        return drawString(text, x - getWidth(text).toFloat() / 2, y - height / 2, color)
    }

    fun getCharWidth(character: Char): Int {
        return Math.round(getCharWidthFloat(character))
    }

    private fun getCharWidthFloat(p_getCharWidthFloat_1_: Char): Float {
        if (p_getCharWidthFloat_1_.code == 167) {
            return -1.0f
        } else if (p_getCharWidthFloat_1_.code != 32 && p_getCharWidthFloat_1_.code != 160) {
            val i =
                "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(
                    p_getCharWidthFloat_1_
                )

            if (p_getCharWidthFloat_1_.code > 0 && i != -1 && !unicodeFlag) {
                return charWidthFloat[i]
            } else if (glyphWidth[p_getCharWidthFloat_1_.code].toInt() != 0) {
                var j = glyphWidth[p_getCharWidthFloat_1_.code].toInt() ushr 4
                var k = glyphWidth[p_getCharWidthFloat_1_.code].toInt() and 15

                if (k > 7) {
                    k = 15
                    j = 0
                }

                ++k
                return ((k - j) / 2 + 1).toFloat()
            } else {
                return 0.0f
            }
        } else {
            return charWidthFloat[32]
        }
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    /**
     * Trims a string to fit a specified Width.
     */
    @JvmOverloads
    fun trimStringToWidth(text: String, width: Int, reverse: Boolean = false): String {
        val stringbuilder = StringBuilder()
        var f = 0.0f
        val i = if (reverse) text.length - 1 else 0
        val j = if (reverse) -1 else 1
        var flag = false
        var flag1 = false

        var k = i
        while (k >= 0 && k < text.length && f < width.toFloat()) {
            val c0 = text[k]
            val f1 = getCharWidthFloat(c0)

            if (flag) {
                flag = false

                if (c0.code != 108 && c0.code != 76) {
                    if (c0.code == 114 || c0.code == 82) {
                        flag1 = false
                    }
                } else {
                    flag1 = true
                }
            } else if (f1 < 0.0f) {
                flag = true
            } else {
                f += f1

                if (flag1) {
                    ++f
                }
            }

            if (f > width.toFloat()) {
                break
            }

            if (reverse) {
                stringbuilder.insert(0, c0)
            } else {
                stringbuilder.append(c0)
            }
            k += j
        }

        return stringbuilder.toString()
    }

    fun drawString(text: String?, x: Double, y: Double, color: Int, shadow: Boolean): Float {
        var x = x
        var y = y
        var color = color
        x -= 1.0
        y -= 0.5

        if (text == null) {
            return 0.0f
        }

        if (color == 553648127) {
            color = 16777215
        }

        if ((color and -0x4000000) == 0) {
            color = color or -16777216
        }

        if (shadow) {
            color = (color and 0xFCFCFC) shr 2 or (color and -0x1000000)
        }

        var currentData = charData

        val alpha = (color shr 24 and 0xFF) / 255.0f

        var bold = false
        var italic = false
        var strike = false
        var underline = false
        val render = true

        x *= 2.0
        y = (y - 5.0) * 2.0

        GL11.glPushMatrix()

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        GL11.glScaled(0.5, 0.5, 0.5)

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(770, 771)

        GL11.glColor4f(
            (color shr 16 and 0xFF) / 255.0f, (color shr 8 and 0xFF) / 255.0f,
            (color and 0xFF) / 255.0f, alpha
        )

        val size = text.length

        GL11.glEnable(3553)

        tex?.let { GL11.glBindTexture(3553, it.glTextureId) }

        var i = 0
        while (i < size) {
            val character = text[i]

            if (character == COLOR_CODE_START) {
                var colorIndex = 21

                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text[i + 1])
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (colorIndex < 16) {
                    bold = false
                    italic = false
                    underline = false
                    strike = false

                    tex?.let { GL11.glBindTexture(GL11.GL_TEXTURE_2D, it.glTextureId) }

                    currentData = charData

                    if (colorIndex < 0) {
                        colorIndex = 15
                    }

                    if (shadow) {
                        colorIndex += 16
                    }

                    val cc = colorCode[colorIndex]
                    GL11.glColor4f(
                        (cc shr 16 and 0xFF) / 255.0f, (cc shr 8 and 0xFF) / 255.0f,
                        (cc and 0xFF) / 255.0f, alpha
                    )
                } else if (colorIndex == 16) {
                } else if (colorIndex == 17) {
                    bold = true

                    if (italic) {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalicBold!!.glTextureId)
                        currentData = boldItalicChars
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texBold!!.glTextureId)
                        currentData = boldChars
                    }
                } else if (colorIndex == 18) {
                    strike = true
                } else if (colorIndex == 19) {
                    underline = true
                } else if (colorIndex == 20) {
                    italic = true

                    if (bold) {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalicBold!!.glTextureId)
                        currentData = boldItalicChars
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalic!!.glTextureId)
                        currentData = italicChars
                    }
                } else if (colorIndex == 21) {
                    bold = false
                    italic = false
                    underline = false
                    strike = false
                    GL11.glColor4f(
                        (color shr 16 and 0xFF) / 255.0f, (color shr 8 and 0xFF) / 255.0f,
                        (color and 0xFF) / 255.0f, alpha
                    )
                    tex?.let { GL11.glBindTexture(GL11.GL_TEXTURE_2D, it.glTextureId) }
                    currentData = charData
                }

                i++
            } else if (character.code < currentData.size) {
                GL11.glBegin(GL11.GL_TRIANGLES)
                drawChar(currentData, character, x.toFloat(), y.toFloat() + 6f)
                GL11.glEnd()

                if (strike) {
                    drawLine(
                        x,
                        y + currentData[character.code].height.toDouble() / 2,
                        x + currentData[character.code].width - 8.0,
                        y + currentData[character.code].height.toDouble() / 2,
                        1.0f
                    )
                }

                if (underline) {
                    drawLine(
                        x,
                        y + currentData[character.code].height - 2.0,
                        x + currentData[character.code].width - 8.0,
                        y + currentData[character.code].height - 2.0,
                        1.0f
                    )
                }

                x += (currentData[character.code].width - 8 + charOffset).toDouble()
            }
            i++
        }

        GL11.glDisable(GL11.GL_BLEND)

        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE)
        GL11.glPopMatrix()

        return x.toFloat() / 2.0f
    }

    fun getWidth(text: String?): Int {
        if (text == null) {
            return 0
        }

        var width = 0
        var currentData = charData
        var bold = false
        var italic = false
        val size = text.length

        var i = 0
        while (i < size) {
            val character = text[i]

            if (character == COLOR_CODE_START) {
                val colorIndex = "0123456789abcdefklmnor".indexOf(character)

                if (colorIndex < 16) {
                    bold = false
                    italic = false
                } else if (colorIndex == 17) {
                    bold = true

                    currentData = if (italic) {
                        boldItalicChars
                    } else {
                        boldChars
                    }
                } else if (colorIndex == 20) {
                    italic = true

                    currentData = if (bold) {
                        boldItalicChars
                    } else {
                        italicChars
                    }
                } else if (colorIndex == 21) {
                    bold = false
                    italic = false
                    currentData = charData
                }

                i++
            } else if (character.code < currentData.size) {
                width += currentData[character.code].width - 8 + charOffset
            }
            i++
        }

        return width / 2
    }

    private fun setupBoldItalicIDs() {
        texBold = setupTexture(font.deriveFont(1), antiAlias, fractionalMetrics, boldChars)
        texItalic = setupTexture(font.deriveFont(2), antiAlias, fractionalMetrics, italicChars)
        texItalicBold = setupTexture(font.deriveFont(3), antiAlias, fractionalMetrics, boldItalicChars)
    }

    private fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x1, y1)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun wrapWords(text: String, width: Double): List<String> {
        val finalWords: MutableList<String> = ArrayList()

        if (getWidth(text) > width) {
            val words = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var currentWord = StringBuilder()
            var lastColorCode = 65535.toChar()

            for (word in words) {
                for (i in word.indices) {
                    val c = word.toCharArray()[i]

                    if ((c == COLOR_CODE_START) && (i < word.length - 1)) {
                        lastColorCode = word.toCharArray()[(i + 1)]
                    }
                }

                if (getWidth("$currentWord$word ") < width) {
                    currentWord.append(word).append(" ")
                } else {
                    finalWords.add(currentWord.toString())
                    currentWord = StringBuilder((COLOR_CODE_START.code + lastColorCode.code).toString() + word + " ")
                }
            }

            if (currentWord.isNotEmpty()) {
                if (getWidth(currentWord.toString()) < width) {
                    finalWords.add((COLOR_CODE_START.code + lastColorCode.code).toString() + currentWord.toString() + " ")
                    currentWord = StringBuilder()
                } else {
                    for (s in formatString(currentWord.toString(), width)) {
                        finalWords.add(s)
                    }
                }
            }
        } else {
            finalWords.add(text)
        }

        return finalWords
    }

    private fun formatString(string: String, width: Double): List<String> {
        val finalWords: MutableList<String> = ArrayList()
        var currentWord = ""
        var lastColorCode = 65535.toChar()
        val chars = string.toCharArray()

        for (i in chars.indices) {
            val c = chars[i]

            if ((c == COLOR_CODE_START) && (i < chars.size - 1)) {
                lastColorCode = chars[(i + 1)]
            }

            if (getWidth(currentWord + c) < width) {
                currentWord += c
            } else {
                finalWords.add(currentWord)
                currentWord = (COLOR_CODE_START.code + lastColorCode.code).toString() + c.toString()
            }
        }

        if (currentWord.isNotEmpty()) {
            finalWords.add(currentWord)
        }

        return finalWords
    }

    private fun setupMinecraftColorCodes() {
        for (index in 0..31) {
            val alpha = (index shr 3 and 0x1) * 85
            var red = (index shr 2 and 0x1) * 170 + alpha
            var green = (index shr 1 and 0x1) * 170 + alpha
            var blue = (index and 0x1) * 170 + alpha

            if (index == 6) {
                red += 85
            }

            if (index >= 16) {
                red /= 4
                green /= 4
                blue /= 4
            }

            colorCode[index] = ((red and 0xFF) shl 16 or ((green and 0xFF) shl 8) or (blue and 0xFF))
        }
    }
}