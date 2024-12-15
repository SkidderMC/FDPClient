/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.*
import java.awt.image.BufferedImage
import java.io.IOException

/**
 * @author opZywl - Custom Font
 */
open class CustomFont(resourceLocation: ResourceLocation?, size: Float) {
    private val imgSize = 1048f
    protected var charData: Array<CharData> = Array(256) { CharData() }
    protected var tex: DynamicTexture?
    protected var font: Font
    protected var antiAlias: Boolean
    protected var fractionalMetrics: Boolean
    protected var fontHeight: Int = -1
    protected var charOffset: Int = 0

    init {
        var tmp: Font
        try {
            val `is` = Minecraft.getMinecraft().resourceManager.getResource(resourceLocation)
                .inputStream
            tmp = Font.createFont(Font.TRUETYPE_FONT, `is`).deriveFont(size)
        } catch (e: IOException) {
            tmp = Font("lato", Font.PLAIN, size.toInt())
            e.printStackTrace()
        } catch (e: FontFormatException) {
            tmp = Font("lato", Font.PLAIN, size.toInt())
            e.printStackTrace()
        }

        this.font = tmp
        this.antiAlias = true
        this.fractionalMetrics = true
        this.tex = this.setupTexture(this.font, antiAlias = true, fractionalMetrics = true, chars = this.charData)
    }

    protected fun setupTexture(
        font: Font,
        antiAlias: Boolean,
        fractionalMetrics: Boolean,
        chars: Array<CharData>
    ): DynamicTexture? {
        val img = generateFontImage(font, antiAlias, fractionalMetrics, chars)

        try {
            return DynamicTexture(img)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun generateFontImage(
        font: Font,
        antiAlias: Boolean,
        fractionalMetrics: Boolean,
        chars: Array<CharData>
    ): BufferedImage {
        val imgSize = imgSize.toInt()
        val bufferedImage = BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB)

        val g = bufferedImage.graphics as Graphics2D
        g.font = font
        g.color = Color(255, 255, 255, 0)
        g.fillRect(0, 0, imgSize, imgSize)
        g.color = Color.WHITE

        g.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            if (fractionalMetrics) RenderingHints.VALUE_FRACTIONALMETRICS_ON else RenderingHints.VALUE_FRACTIONALMETRICS_OFF
        )
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            if (antiAlias) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
        )
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            if (antiAlias) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
        )

        val fontMetrics = g.fontMetrics
        var charHeight = 0
        var positionX = 0
        var positionY = 1

        for (i in chars.indices) {
            val ch = i.toChar()
            val charData = CharData()
            val dimensions = fontMetrics.getStringBounds(ch.toString(), g)

            charData.width = (dimensions.bounds.width + 8)
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

            if (charData.height > this.fontHeight) {
                this.fontHeight = charData.height
            }

            chars[i] = charData
            g.drawString(ch.toString(), positionX + 2, positionY + fontMetrics.ascent)
            positionX += charData.width
        }

        return bufferedImage
    }

    protected fun drawChar(chars: Array<CharData>, c: Char, x: Float, y: Float) {
        drawQuad(
            x,
            y,
            chars[c.code].width.toFloat(),
            chars[c.code].height.toFloat(),
            chars[c.code].storedX.toFloat(),
            chars[c.code].storedY.toFloat(),
            chars[c.code].width.toFloat(),
            chars[c.code].height.toFloat()
        )
    }

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
        val renderSRCX = srcX / imgSize
        val renderSRCY = srcY / imgSize
        val renderSRCWidth = srcWidth / imgSize
        val renderSRCHeight = srcHeight / imgSize

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

    val height: Int
        get() = (this.fontHeight - 8) / 2

    protected class CharData {
        var width: Int = 0
        var height: Int = 0
        var storedX: Int = 0
        var storedY: Int = 0
    }
}