package net.skiddermc.fdpclient.ui.font.renderer

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.ui.font.FontsGC
import net.skiddermc.fdpclient.ui.font.renderer.glyph.GlyphFontRenderer
import net.skiddermc.fdpclient.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Canvas
import java.awt.Font

abstract class AbstractAwtFontRender(val font: Font) {

    protected val fontMetrics = Canvas().getFontMetrics(font)
    protected open val fontHeight = if (fontMetrics.height <= 0) { font.size } else { fontMetrics.height + 3 }
    open val height: Int
        get() = (fontHeight - 8) / 2

    protected val cachedChars = mutableMapOf<String, AbstractCachedFont>()

    /**
     * Allows you to draw a string with the target font
     *
     * @param text  to render
     * @param x     location for target position
     * @param y     location for target position
     * @param color of the text
     */
    open fun drawString(text: String, x: Double, y: Double, color: Int) {
        val scale = 0.25

        GL11.glPushMatrix()
        GL11.glScaled(scale, scale, scale)
        GL11.glTranslated(x * 2F, y * 2.0 - 2.0, 0.0)
        RenderUtils.glColor(color)

        text.forEach { // this is faster than toCharArray()
            GL11.glTranslatef(drawChar(it.toString()).toFloat(), 0f, 0f)
        }

        GL11.glPopMatrix()
    }

    /**
     * Draw char from texture to display
     *
     * @param char target font char to render
     * @param x        target position x to render
     * @param y        target position y to render
     */
    abstract fun drawChar(char: String): Int

    /**
     * Get the width of a string
     */
    open fun getStringWidth(text: String) = fontMetrics.stringWidth(text) / 2

//    /**
//     * Get the width of a char
//     */
//    open fun getCharWidth(char: String) = fontMetrics.stringWidth(char) / 2

    /**
     * prepare gl hints before render
     */
    abstract fun preGlHints()

    /**
     * prepare gl hints after render
     */
    abstract fun postGlHints()

    /**
     * collect useless garbage to save memory
     */
    open fun collectGarbage() {
        val currentTime = System.currentTimeMillis()

        cachedChars.filter { currentTime - it.value.lastUsage > FontsGC.CACHED_FONT_REMOVAL_TIME }.forEach {
            it.value.finalize()

            cachedChars.remove(it.key)
        }
    }

    /**
     * delete all cache
     */
    open fun close() {
        cachedChars.forEach { (_, cachedFont) -> cachedFont.finalize() }
        cachedChars.clear()
    }

    companion object {
        fun build(font: Font): AbstractAwtFontRender {
            return if (FDPClient.fileManager.specialConfig.useGlyphFontRenderer) {
                GlyphFontRenderer(font)
            } else {
                //VectorFontRenderer(font)
                GlyphFontRenderer(font)
            }
        }
    }
}
