/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.utils.io.flipSafely
import net.ccbluex.liquidbounce.utils.render.drawWithTessellatorWorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

/**
 * Texture rendering and management utilities
 *
 * @author Zywl
 */
object RenderTexture {

    data class ColorValueCache(val lastHue: Float, val cachedTextureID: Int)

    private val colorValueCache: MutableMap<ColorValue, MutableMap<Int, ColorValueCache>> = mutableMapOf()

    /**
     * Draws a textured rectangle at z = 0 with custom texture coordinates.
     *
     * @param x X position
     * @param y Y position
     * @param u Texture U coordinate
     * @param v Texture V coordinate
     * @param width Rectangle width
     * @param height Rectangle height
     * @param textureWidth Full texture width
     * @param textureHeight Full texture height
     */
    @JvmStatic
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 1f / textureWidth
        val f1 = 1f / textureHeight
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), 0.0).tex((u * f).toDouble(), ((v + height) * f1).toDouble())
            .endVertex()
        pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((u + width) * f).toDouble(), ((v + height) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + width) * f).toDouble(), (v * f1).toDouble())
            .endVertex()
        pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
    }

    /**
     * Draws a textured rectangle at the stored z-value.
     *
     * @param x X position
     * @param y Y position
     * @param textureX Texture X coordinate
     * @param textureY Texture Y coordinate
     * @param width Rectangle width
     * @param height Rectangle height
     * @param zLevel Z-level for depth
     */
    @JvmStatic
    fun drawTexturedModalRect(
        x: Int,
        y: Int,
        textureX: Int,
        textureY: Int,
        width: Int,
        height: Int,
        zLevel: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 0.00390625f
        val f1 = 0.00390625f
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()
        ).endVertex()
        pos(
            (x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble()
        ).tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        pos((x + width).toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()
        ).endVertex()
        pos(x.toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()
        ).endVertex()
    }

    /**
     * Draws a scaled custom size modal rectangle with texture coordinates.
     *
     * @param x X position
     * @param y Y position
     * @param u Texture U coordinate
     * @param v Texture V coordinate
     * @param uWidth Width in texture coordinates
     * @param vHeight Height in texture coordinates
     * @param width Rendered width
     * @param height Rendered height
     * @param tileWidth Full tile width
     * @param tileHeight Full tile height
     */
    @JvmStatic
    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) = drawWithTessellatorWorldRenderer {
        val f = 1f / tileWidth
        val f1 = 1f / tileHeight
        begin(7, DefaultVertexFormats.POSITION_TEX)
        pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(
            (u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()
        ).endVertex()
        pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble())
            .endVertex()
        pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
    }

    /**
     * Draws a texture by its texture ID.
     *
     * @param textureID OpenGL texture ID
     * @param x X position
     * @param y Y position
     * @param width Width to render
     * @param height Height to render
     */
    @JvmStatic
    fun drawTexture(textureID: Int, x: Int, y: Int, width: Int, height: Int) {
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glDisable(GL_CULL_FACE)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0f)

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTranslatef(x.toFloat(), y.toFloat(), 0.0f)

        glBegin(GL_QUADS)
        glTexCoord2f(0.0f, 0.0f); glVertex2f(0.0f, 0.0f)
        glTexCoord2f(1.0f, 0.0f); glVertex2f(width.toFloat(), 0.0f)
        glTexCoord2f(1.0f, 1.0f); glVertex2f(width.toFloat(), height.toFloat())
        glTexCoord2f(0.0f, 1.0f); glVertex2f(0.0f, height.toFloat())
        glEnd()

        glDisable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_CULL_FACE)

        glPopMatrix()
        glPopAttrib()
    }

    /**
     * Updates and caches texture for a ColorValue.
     *
     * @param colorValue The ColorValue to cache for
     * @param id Cache identifier
     * @param hue Current hue value
     * @param width Texture width
     * @param height Texture height
     * @param generateImage Function to generate the image
     * @param drawAt Function to draw with the texture ID
     */
    @JvmStatic
    fun ColorValue.updateTextureCache(
        id: Int,
        hue: Float,
        width: Int,
        height: Int,
        generateImage: (BufferedImage, Graphics2D) -> Unit,
        drawAt: (Int) -> Unit
    ) {
        val cached = colorValueCache[this]?.get(id)
        val lastHue = cached?.lastHue

        if (lastHue == null || lastHue != hue) {
            val image = createRGBImageDrawing(width, height) { img, graphics -> generateImage(img, graphics) }
            val texture = convertImageToTexture(image)
            colorValueCache.getOrPut(this, ::mutableMapOf)[id] = ColorValueCache(hue, texture)
        }

        colorValueCache[this]?.get(id)?.cachedTextureID?.let(drawAt)
    }

    /**
     * Creates a BufferedImage and executes drawing operations on it.
     *
     * @param width Image width
     * @param height Image height
     * @param f Drawing function
     * @return Created BufferedImage
     */
    @JvmStatic
    fun createRGBImageDrawing(width: Int, height: Int, f: (BufferedImage, Graphics2D) -> Unit): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        f(image, g)

        g.dispose()
        return image
    }

    /**
     * Converts a BufferedImage to an OpenGL texture.
     *
     * @param image The image to convert
     * @return OpenGL texture ID
     */
    @JvmStatic
    fun convertImageToTexture(image: BufferedImage): Int {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)

        image.getRGB(0, 0, width, height, pixels, 0, width)

        val buffer = ByteBuffer.allocateDirect(width * height * 4)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put(((pixel shr 0) and 0xFF).toByte())
            buffer.put(((pixel shr 24) and 0xFF).toByte())
        }

        buffer.flipSafely()

        val textureID = glGenTextures()

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        glPopMatrix()
        glPopAttrib()

        return textureID
    }

    /**
     * Clears the color value texture cache.
     */
    @JvmStatic
    fun clearTextureCache() {
        colorValueCache.clear()
    }
}
