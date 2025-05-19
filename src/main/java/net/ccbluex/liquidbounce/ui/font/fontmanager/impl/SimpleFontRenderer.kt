/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.impl

import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import kotlin.math.max

@Suppress("MagicNumber")
class SimpleFontRenderer(
    private val awtFont: Font,
    private val antiAlias: Boolean,
    private val fractionalMetrics: Boolean
) : FontRenderer {

    val charData: Array<CharData> = Array(CHARS.toInt()) { CharData() }
    private val boldChars: Array<CharData> = Array(CHARS.toInt()) { CharData() }
    private val italicChars: Array<CharData> = Array(CHARS.toInt()) { CharData() }
    private val boldItalicChars: Array<CharData> = Array(CHARS.toInt()) { CharData() }

    private lateinit var texturePlain: DynamicTexture
    private lateinit var textureBold: DynamicTexture
    private lateinit var textureItalic: DynamicTexture
    private lateinit var textureItalicBold: DynamicTexture
    private var fontHeight: Int = -1

    init {
        setupBoldItalicFonts()
    }

    companion object {
        private val COLOR_CODES = setupMinecraftColorCodes()
        private const val COLORS = "0123456789abcdefklmnor"
        private const val COLOR_PREFIX = '\u00a7'
        private const val CHARS: Short = 256
        private const val IMG_SIZE: Float = 512f
        private const val CHAR_OFFSET: Float = 0f

        /**
         * Creates an instance of [SimpleFontRenderer] with specified settings.
         *
         * @param font The AWT font to be used.
         * @param antiAlias Indicates if anti-aliasing should be applied.
         * @param fractionalMetrics Indicates if fractional metrics should be used.
         * @return A new instance of [SimpleFontRenderer].
         */
        fun create(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean): FontRenderer =
            SimpleFontRenderer(font, antiAlias, fractionalMetrics)

        /**
         * Creates an instance of [SimpleFontRenderer] with anti-aliasing and fractional metrics enabled by default.
         *
         * @param font The AWT font to be used.
         * @return A new instance of [SimpleFontRenderer].
         */
        fun create(font: Font): FontRenderer =
            create(font, true, true)

        /**
         * Sets up Minecraft color codes.
         *
         * @return An array of color codes.
         */
        private fun setupMinecraftColorCodes(): IntArray {
            val colorCodes = IntArray(32)

            for (i in 0 until 32) {
                var noClue = ((i shr 3) and 0x1) * 85
                var red = ((i shr 2) and 0x1) * 170 + noClue
                var green = ((i shr 1) and 0x1) * 170 + noClue
                var blue = (i and 0x1) * 170 + noClue

                if (i == 6) {
                    red += 85
                }

                if (i >= 16) {
                    red = (red shr 2) and 0xFF
                    green = (green shr 2) and 0xFF
                    blue = (blue shr 2) and 0xFF
                }

                colorCodes[i] = (red shl 16) or (green shl 8) or blue
            }

            return colorCodes
        }
    }

    /**
     * Sets up textures for different font styles (plain, bold, italic, bold-italic).
     */
    private fun setupBoldItalicFonts() {
        texturePlain = setupTexture(awtFont, antiAlias, fractionalMetrics, charData)
        textureBold = setupTexture(awtFont.deriveFont(Font.BOLD), antiAlias, fractionalMetrics, boldChars)
        textureItalic = setupTexture(awtFont.deriveFont(Font.ITALIC), antiAlias, fractionalMetrics, italicChars)
        textureItalicBold = setupTexture(awtFont.deriveFont(Font.BOLD or Font.ITALIC), antiAlias, fractionalMetrics, boldItalicChars)
    }

    /**
     * Sets up a texture from the specified font and settings.
     *
     * @param font The AWT font to be used.
     * @param antiAlias Indicates if anti-aliasing should be applied.
     * @param fractionalMetrics Indicates if fractional metrics should be used.
     * @param chars The array of [CharData] to be populated.
     * @return A new instance of [DynamicTexture].
     */
    private fun setupTexture(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean, chars: Array<CharData>): DynamicTexture =
        DynamicTexture(generateFontImage(font, antiAlias, fractionalMetrics, chars))

    /**
     * Generates a font image with mapped characters.
     *
     * @param font The AWT font to be used.
     * @param antiAlias Indicates if anti-aliasing should be applied.
     * @param fractionalMetrics Indicates if fractional metrics should be used.
     * @param chars The array of [CharData] to be populated.
     * @return A [BufferedImage] containing the rendered characters.
     */
    private fun generateFontImage(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean, chars: Array<CharData>): BufferedImage {
        val imgSize = IMG_SIZE.toInt()
        val bufferedImage = BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics() as Graphics2D

        graphics.font = font
        graphics.color = Color(255, 255, 255, 0)
        graphics.fillRect(0, 0, imgSize, imgSize)
        graphics.color = Color.WHITE

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        graphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            if (fractionalMetrics) RenderingHints.VALUE_FRACTIONALMETRICS_ON else RenderingHints.VALUE_FRACTIONALMETRICS_OFF
        )

        val fontMetrics = graphics.fontMetrics
        var charHeight = 0
        var positionX = 0
        var positionY = 1

        for (i in chars.indices) {
            val ch = i.toChar()
            val charData = CharData()
            val dimensions: Rectangle2D = fontMetrics.getStringBounds(ch.toString(), graphics)

            charData.width = dimensions.bounds.width + 8
            charData.height = dimensions.bounds.height

            if (positionX + charData.width >= imgSize) {
                positionX = 0
                positionY += charHeight
                charHeight = 0
            }

            if (charData.height > charHeight) {
                charHeight = charData.height
            }

            charData.storedX = positionX
            charData.storedY = positionY

            if (charData.height > fontHeight) {
                fontHeight = charData.height
            }

            chars[i] = charData

            graphics.drawString(ch.toString(), positionX + 2, positionY + fontMetrics.ascent)
            positionX += charData.width
        }

        graphics.dispose()

        return bufferedImage
    }

    override fun drawString(text: CharSequence, x: Double, y: Double, color: Int, dropShadow: Boolean): Float {
        return if (dropShadow) {
            val shadowWidth = drawStringInternal(text, x + 0.5, y + 0.5, color, true)
            max(shadowWidth, drawStringInternal(text, x, y, color, false))
        } else {
            drawStringInternal(text, x, y, color, false)
        }
    }

    override fun drawString(text: CharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean): Float {
        return if (dropShadow) {
            val shadowWidth = drawStringInternal(text, (x + 0.5f).toDouble(), (y + 0.5f).toDouble(), color, true)
            max(shadowWidth, drawStringInternal(text, x.toDouble(), y.toDouble(), color, false))
        } else {
            drawStringInternal(text, x.toDouble(), y.toDouble(), color, false)
        }
    }

    /**
     * Internal method to draw the string using OpenGL context.
     *
     * @param text The string to be drawn.
     * @param x The initial X position.
     * @param y The initial Y position.
     * @param color The color of the font.
     * @param shadow Indicates if a shadow should be drawn.
     * @return The total width of the drawn string.
     */
    private fun drawStringInternal(text: CharSequence, x: Double, y: Double, color: Int, shadow: Boolean): Float {
        var xPos = x - 1.0
        var modifiedColor = color

        if (color == 0x20FFFFFF) modifiedColor = 0xFFFFFF
        if ((modifiedColor and 0xFC000000.toInt()) == 0) {
            modifiedColor = modifiedColor or 0xFF000000.toInt()
        }

        if (shadow) {
            modifiedColor = ((modifiedColor and 0xFCFCFC) shr 2) or (modifiedColor and 0xFF000000.toInt())
        }

        var currentCharData = charData
        val alpha = ((modifiedColor shr 24) and 0xFF) / 255.0f

        var xScaled = xPos * 2.0
        var yScaled = (y - 3.0) * 2.0

        GL11.glPushMatrix()
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        GL11.glColor4f(
            ((modifiedColor shr 16) and 0xFF) / 255.0f,
            ((modifiedColor shr 8) and 0xFF) / 255.0f,
            (modifiedColor and 0xFF) / 255.0f,
            alpha
        )
        GlStateManager.color(
            ((modifiedColor shr 16) and 0xFF) / 255.0f,
            ((modifiedColor shr 8) and 0xFF) / 255.0f,
            (modifiedColor and 0xFF) / 255.0f,
            alpha
        )
        GlStateManager.enableTexture2D()
        GlStateManager.bindTexture(texturePlain.glTextureId)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePlain.glTextureId)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR.toFloat())

        var underline = false
        var strikethrough = false
        var italic = false
        var bold = false

        var i = 0
        while (i < text.length) {
            val character = text[i]

            if (character == COLOR_PREFIX && i + 1 < text.length) {
                val colorChar = text[i + 1]
                val colorIndex = COLORS.indexOf(colorChar)

                when {
                    colorIndex < 16 -> { // Color
                        bold = false
                        italic = false
                        underline = false
                        strikethrough = false
                        GlStateManager.bindTexture(texturePlain.glTextureId)
                        currentCharData = charData

                        var ci = colorIndex
                        if (ci < 0) ci = 15
                        if (shadow) ci += 16

                        val colorCode = COLOR_CODES.getOrElse(ci) { COLOR_CODES[15] }

                        GlStateManager.color(
                            ((colorCode shr 16) and 0xFF) / 255.0f,
                            ((colorCode shr 8) and 0xFF) / 255.0f,
                            (colorCode and 0xFF) / 255.0f,
                            1.0f
                        )
                    }
                    colorIndex == 17 -> { // Bold
                        bold = true
                        currentCharData = if (italic) boldItalicChars else boldChars
                        GlStateManager.bindTexture(if (italic) textureItalicBold.glTextureId else textureBold.glTextureId)
                    }
                    colorIndex == 18 -> { // Strikethrough
                        strikethrough = true
                    }
                    colorIndex == 19 -> { // Underline
                        underline = true
                    }
                    colorIndex == 20 -> { // Italic
                        italic = true
                        currentCharData = if (bold) boldItalicChars else italicChars
                        GlStateManager.bindTexture(if (bold) textureItalicBold.glTextureId else textureItalic.glTextureId)
                    }
                    colorIndex == 21 -> { // Reset
                        bold = false
                        italic = false
                        underline = false
                        strikethrough = false

                        GlStateManager.color(
                            ((modifiedColor shr 16) and 0xFF) / 255.0f,
                            ((modifiedColor shr 8) and 0xFF) / 255.0f,
                            (modifiedColor and 0xFF) / 255.0f,
                            1.0f
                        )
                        GlStateManager.bindTexture(texturePlain.glTextureId)

                        currentCharData = charData
                    }
                }

                i++ // Skip color character
            } else if (character.code < currentCharData.size) {
                GL11.glBegin(GL11.GL_TRIANGLES)
                drawChar(currentCharData, character, xScaled.toFloat(), yScaled.toFloat())
                GL11.glEnd()

                if (strikethrough) {
                    drawLine(
                        xScaled,
                        yScaled + currentCharData[character.code].height / 2.0f,
                        xScaled + (currentCharData[character.code].width - 8).toDouble(),
                        yScaled + currentCharData[character.code].height / 2.0f,
                        1.0f
                    )
                }

                if (underline) {
                    drawLine(
                        xScaled,
                        yScaled + currentCharData[character.code].height - 2.0,
                        xScaled + (currentCharData[character.code].width - 8).toDouble(),
                        yScaled + currentCharData[character.code].height - 2.0,
                        1.0f
                    )
                }

                xScaled += (currentCharData[character.code].width - if (character == ' ') 8 else 9).toDouble()
            }

            i++
        }

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST.toFloat())
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE)
        GL11.glPopMatrix()

        return (xScaled / 2.0).toFloat()
    }

    override fun trimStringToWidth(text: CharSequence, width: Int, reverse: Boolean): String {
        val builder = StringBuilder()
        var accumulatedWidth = 0.0f
        var index = if (reverse) text.length - 1 else 0
        val step = if (reverse) -1 else 1
        var skipNext = false
        var bold = false
        var italic = false

        while (index >= 0 && index < text.length && accumulatedWidth < width) {
            val currentChar = text[index]
            if (skipNext) {
                skipNext = false
                when (currentChar.toLowerCase()) {
                    'l' -> bold = true
                    'r' -> {
                        bold = false
                        italic = false
                    }
                }
            } else if (currentChar == COLOR_PREFIX) {
                skipNext = true
            } else {
                val charWidth = stringWidth(currentChar.toString())
                accumulatedWidth += charWidth
                if (bold) accumulatedWidth += 1

                if (accumulatedWidth > width) break

                if (reverse) {
                    builder.insert(0, currentChar)
                } else {
                    builder.append(currentChar)
                }
            }

            index += step
        }

        return builder.toString()
    }

    override fun stringWidth(text: CharSequence): Int {
        var width = 0
        var currentData = charData
        var bold = false
        var italic = false

        var i = 0
        while (i < text.length) {
            val character = text[i]

            if (character == COLOR_PREFIX && i + 1 < text.length) {
                val colorChar = text[i + 1]
                val colorIndex = COLORS.indexOf(colorChar)

                when {
                    colorIndex < 16 -> { // Color
                        bold = false
                        italic = false
                    }
                    colorIndex == 17 -> { // Bold
                        bold = true
                        currentData = if (italic) boldItalicChars else boldChars
                    }
                    colorIndex == 20 -> { // Italic
                        italic = true
                        currentData = if (bold) boldItalicChars else italicChars
                    }
                    colorIndex == 21 -> { // Reset
                        bold = false
                        italic = false
                        currentData = charData
                    }
                }

                i += 2 // Skip color character
                continue
            }

            if (character.code < currentData.size) {
                width += currentData[character.code].width - if (character == ' ') 8 else 9
            }

            i++
        }

        return width / 2
    }

    override fun charWidth(s: Char): Float {
        return ((charData[s.code].width - 8).toFloat()) / 2
    }

    /**
     * Draws a character on the screen.
     *
     * @param chars The array of [CharData] corresponding to the current style.
     * @param c The character to be drawn.
     * @param x The X position.
     * @param y The Y position.
     */
    private fun drawChar(chars: Array<CharData>, c: Char, x: Float, y: Float) {
        val charIndex = c.code
        if (charIndex >= chars.size) return
        val charData = chars[charIndex]
        drawQuad(
            x,
            y,
            charData.width.toFloat(),
            charData.height.toFloat(),
            charData.storedX.toFloat(),
            charData.storedY.toFloat(),
            charData.width.toFloat(),
            charData.height.toFloat()
        )
    }

    /**
     * Draws a textured quad.
     *
     * @param x The X position.
     * @param y The Y position.
     * @param width The width of the quad.
     * @param height The height of the quad.
     * @param srcX The X position on the texture.
     * @param srcY The Y position on the texture.
     * @param srcWidth The width of the texture section.
     * @param srcHeight The height of the texture section.
     */
    private fun drawQuad(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        srcX: Float,
        srcY: Float,
        srcWidth: Float,
        srcHeight: Float
    ) {
        val renderSRCX = srcX / IMG_SIZE
        val renderSRCY = srcY / IMG_SIZE
        val renderSRCWidth = srcWidth / IMG_SIZE
        val renderSRCHeight = srcHeight / IMG_SIZE

        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY)
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())

        GL11.glTexCoord2f(renderSRCX, renderSRCY)
        GL11.glVertex2d(x.toDouble(), y.toDouble())

        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight)
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())

        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight)
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())

        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight)
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())

        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY)
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
    }

    /**
     * Draws a line on the screen.
     *
     * @param x The starting X position.
     * @param y The starting Y position.
     * @param x1 The ending X position.
     * @param y1 The ending Y position.
     * @param width The width of the line.
     */
    private fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x1, y1)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    /**
     * Internal class to store data for each character.
     */
    class CharData {
        var width: Int = 0
        var height: Int = 0
        var storedX: Int = 0
        var storedY: Int = 0
    }

    override val name: String
        get() = awtFont.family

    override val height: Int
        get() = (fontHeight - 8) / 2

    override val isAntiAlias: Boolean
        get() = antiAlias

    override val isFractionalMetrics: Boolean
        get() = fractionalMetrics
}