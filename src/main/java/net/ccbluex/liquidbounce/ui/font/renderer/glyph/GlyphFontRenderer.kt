package net.ccbluex.liquidbounce.ui.font.renderer.glyph

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractAwtFontRender
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class GlyphFontRenderer(font: Font) : AbstractAwtFontRender(font) {

    private val fontName = "${font.fontName.replace(" ","_").lowercase()}${if (font.isBold){"-bold"}else {""}}${if (font.isItalic){"-italic"}else {""}}-${font.size}"

    private fun charInfo(char: String): String {
        val charArr = char.toCharArray()
        if (char.length == 1) {
            return "char-${charArr[0].code}"
        } else if (char.length == 2 && charArr[0] in '\ud800'..'\udfff' && charArr[1] in '\ud800'..'\udfff') {
            val first = (charArr[0].code - 0xd800) * 0x400
            val second = charArr[1].code - 0xdc00
            return "char-${first + second + 0x10000}"
        }
        throw IllegalStateException("The char $char not UTF-8 or UTF-16")
    }

    /**
     * @return 通过Char获取的ResourceLocation
     */
    private fun getResourceLocationByChar(char: String) = ResourceLocation("fdp/font/$fontName/${charInfo(char)}")

    /**
     * 渲染字符图片
     */
    private fun renderCharImage(char: String): CachedGlyphFont {
        val charWidth = fontMetrics.stringWidth(char)

        val fontImage = BufferedImage(charWidth, fontHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = fontImage.graphics as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.font = font
        graphics.color = Color.WHITE
        graphics.drawString(char, 0, 1 + fontMetrics.ascent)

        val mc = Minecraft.getMinecraft()
        val resourceLocation = getResourceLocationByChar(char)
        mc.addScheduledTask {
            mc.textureManager.loadTexture(resourceLocation, DynamicTexture(fontImage))
        }

        return CachedGlyphFont(resourceLocation, charWidth)
    }

    override fun drawChar(char: String): Int {
        val cached = if (cachedChars.containsKey(char)) {
            val cached = cachedChars[char]!! as CachedGlyphFont
            cached.lastUsage = System.currentTimeMillis()
            cached
        } else {
            val cached = renderCharImage(char)
            cachedChars[char] = cached
            cached
        }

        Minecraft.getMinecraft().textureManager.bindTexture(cached.resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, cached.width, fontHeight, cached.width.toFloat(), fontHeight.toFloat())

        return cached.width
    }

    override fun preGlHints() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableTexture2D()
    }

    override fun postGlHints() {
        GlStateManager.disableBlend()
    }
}