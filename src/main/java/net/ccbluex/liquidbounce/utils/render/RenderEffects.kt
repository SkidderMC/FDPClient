/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import com.jhlabs.image.GaussianFilter
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.shader.UIEffectRenderer.drawTexturedRect
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.*

/**
 * Effects rendering utilities including shadows, glows, and blur effects
 *
 * @author Zywl
 */
object RenderEffects : MinecraftInstance {

    private val shadowCache = net.ccbluex.liquidbounce.utils.kotlin.LruCache<Int, Int>(50)

    /**
     * Draws a rounded shadow rectangle with specified parameters.
     *
     * @param paramXStart The x-coordinate of the starting point
     * @param paramYStart The y-coordinate of the starting point
     * @param paramXEnd The x-coordinate of the ending point
     * @param paramYEnd The y-coordinate of the ending point
     * @param radius The corner radius
     * @param color The color in ARGB format
     */
    @JvmStatic
    @JvmOverloads
    fun drawShadowRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean = true
    ) {
        var xStart = paramXStart
        var yStart = paramYStart
        var xEnd = paramXEnd
        var yEnd = paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        if (xStart > xEnd) {
            val temp = xStart
            xStart = xEnd
            xEnd = temp
        }

        if (yStart > yEnd) {
            val temp = yStart
            yStart = yEnd
            yEnd = temp
        }

        val x1 = (xStart + radius).toDouble()
        val y1 = (yStart + radius).toDouble()
        val x2 = (xEnd - radius).toDouble()
        val y2 = (yEnd - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180

        var i = 0.0
        while (i <= 90) {
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        i = 90.0
        while (i <= 180) {
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
            i += 1.0
        }

        i = 180.0
        while (i <= 270) {
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
            i += 1.0
        }

        i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    /**
     * Draws a bloom effect with a specified blur radius and color.
     *
     * This method creates a blurred rectangle that simulates a glow effect
     * around the specified area.
     *
     * @param x The x-coordinate of the rectangle's top-left corner
     * @param y The y-coordinate of the rectangle's top-left corner
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param blurRadius The radius of the blur applied to the edges
     * @param color The color used for the bloom effect
     */
    @JvmStatic
    fun drawBloom(x: Int, y: Int, width: Int, height: Int, blurRadius: Int, color: Color) {
        var xPos = x
        var yPos = y
        var w = width
        var h = height

        Gui.drawRect(0, 0, 0, 0, 0)
        pushAttrib()
        pushMatrix()
        alphaFunc(516, 0.01f)

        h = max(0.0, h.toDouble()).toInt()
        w = max(0.0, w.toDouble()).toInt()
        w += blurRadius * 2
        h += blurRadius * 2
        xPos -= blurRadius
        yPos -= blurRadius

        val adjustedX = xPos - 0.25f
        val adjustedY = yPos + 0.25f
        val identifier = w * h + w + color.hashCode() * blurRadius + blurRadius

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)

        if (shadowCache.containsKey(identifier)) {
            val texId: Int = shadowCache.get(identifier)!!
            bindTexture(texId)
        } else {
            val original = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
            val g = original.graphics
            g.color = color
            g.fillRect(blurRadius, blurRadius, w - blurRadius * 2, h - blurRadius * 2)
            g.dispose()

            val op = GaussianFilter(blurRadius.toFloat())
            val blurred = op.filter(original, null)
            val texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false)
            shadowCache[identifier] = texId
        }

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glBegin(GL_QUADS)
        glTexCoord2f(0.0f, 0.0f)
        glVertex2d(adjustedX.toDouble(), adjustedY.toDouble())
        glTexCoord2f(0.0f, 1.0f)
        glVertex2d(adjustedX.toDouble(), (adjustedY + h).toDouble())
        glTexCoord2f(1.0f, 1.0f)
        glVertex2d((adjustedX + w).toDouble(), (adjustedY + h).toDouble())
        glTexCoord2f(1.0f, 0.0f)
        glVertex2d((adjustedX + w).toDouble(), adjustedY.toDouble())
        glEnd()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_CULL_FACE)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_BLEND)
        popAttrib()
        popMatrix()
    }

    /**
     * Draws a shadow effect around a rectangular area using textured rectangles.
     *
     * @param x The x-coordinate of the top-left corner
     * @param y The y-coordinate of the top-left corner
     * @param width The width of the rectangular area
     * @param height The height of the rectangular area
     */
    @JvmStatic
    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9F, 9F, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9F, 9F, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9F, 9F, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9F, 9F, "paneltopright")
        drawTexturedRect(x - 9, y, 9F, height, "panelleft")
        drawTexturedRect(x + width, y, 9F, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9F, "paneltop")
        drawTexturedRect(x, y + height, width, 9F, "panelbottom")
    }

    /**
     * Draws a fake circle glow effect with gradient alpha.
     *
     * @param posX Center x-coordinate of the glow
     * @param posY Center y-coordinate of the glow
     * @param radius Radius of the glow effect
     * @param color Color of the glow
     * @param maxAlpha Maximum alpha value at the center
     */
    @JvmStatic
    fun fakeCircleGlow(posX: Float, posY: Float, radius: Float, color: Color, maxAlpha: Float) {
        RenderUtils.setAlphaLimit(0f)
        glShadeModel(GL_SMOOTH)
        RenderUtils.setup2DRenderingGLUtil {
            RenderUtils.renderGLUtil(GL_TRIANGLE_FAN) {
                RenderUtils.color(color.rgb, maxAlpha)
                glVertex2d(posX.toDouble(), posY.toDouble())
                RenderUtils.color(color.rgb, 0f)
                for (i in 0..100) {
                    val angle = (i * 0.06283) + 3.1415
                    val x2 = sin(angle) * radius
                    val y2 = cos(angle) * radius
                    glVertex2d(posX + x2, posY + y2)
                }
            }
        }
        glShadeModel(GL_FLAT)
        RenderUtils.setAlphaLimit(1f)
    }

    /**
     * Clears the shadow texture cache.
     */
    @JvmStatic
    fun clearShadowCache() {
        shadowCache.clear()
    }
}
