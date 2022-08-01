package net.ccbluex.liquidbounce.ui.font.renderer.glyph

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractAwtFontRender
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class GlyphFontRenderer(font: Font) : AbstractAwtFontRender(font) {

    /**
     * 渲染字符图片
     */
    private fun renderCharImage(char: String): CachedGlyphFont {
        val charWidth = fontMetrics.stringWidth(char)

        val image = BufferedImage(charWidth, fontHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        graphics.font = font
        graphics.paint = Color.WHITE
        graphics.drawString(char, 0, fontMetrics.ascent)
        graphics.dispose()

        return CachedGlyphFont(RenderUtils.loadGlTexture(image), charWidth)
    }

    private fun renderAndCacheTexture(char: String): CachedGlyphFont {
        val cached = renderCharImage(char)
        cachedChars[char] = cached
        return cached
    }

    override fun drawChar(char: String): Int {
        val cached = if (cachedChars.containsKey(char)) {
            val cached = cachedChars[char]!! as CachedGlyphFont
            cached.lastUsage = System.currentTimeMillis()
            cached
        } else {
            renderAndCacheTexture(char)
        }

        val originalTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, cached.tex)
        GL11.glBegin(GL11.GL_QUADS)

        GL11.glTexCoord2d(1.0, 0.0)
        GL11.glVertex2d(cached.width.toDouble(), 0.0)
        GL11.glTexCoord2d(0.0, 0.0)
        GL11.glVertex2d(0.0, 0.0)
        GL11.glTexCoord2d(0.0, 1.0)
        GL11.glVertex2d(0.0, fontHeight.toDouble())
        GL11.glTexCoord2d(1.0, 1.0)
        GL11.glVertex2d(cached.width.toDouble(), fontHeight.toDouble())

        GL11.glEnd()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTex)

        return cached.width
    }

    override fun preGlHints() {
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        RenderUtils.clearCaps()
        RenderUtils.disableGlCap(GL11.GL_DEPTH_TEST)
    }

    override fun postGlHints() {
        RenderUtils.resetCaps()
        GL11.glDisable(GL11.GL_BLEND)
    }
}