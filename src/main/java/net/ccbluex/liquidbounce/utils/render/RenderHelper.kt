/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*

/**
 * Rendering utility helper functions
 *
 * @author Zywl
 */
object RenderHelper : MinecraftInstance {

    /**
     * Creates a scissor box for clipping rendering.
     *
     * @param x X position
     * @param y Y position
     * @param x2 X end position
     * @param y2 Y end position
     */
    @JvmStatic
    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }

    /**
     * Sets up scissor box for clipping with dimensions.
     *
     * @param x X position
     * @param y Y position
     * @param width Width
     * @param height Height
     */
    @JvmStatic
    fun scissor(x: Double, y: Double, width: Double, height: Double) {
        val sr = ScaledResolution(mc)
        val scale = sr.scaleFactor.toDouble()
        val finalHeight = height * scale
        val finalY = (sr.scaledHeight - y) * scale
        val finalX = x * scale
        val finalWidth = width * scale
        glScissor(finalX.toInt(), (finalY - finalHeight).toInt(), finalWidth.toInt(), finalHeight.toInt())
    }

    /**
     * Scales rendering around a point.
     *
     * @param x Center X position
     * @param y Center Y position
     * @param scale Scale factor
     * @param runnable Code to run while scaled
     */
    @JvmStatic
    fun scale(x: Float, y: Float, scale: Float, runnable: Runnable) {
        glPushMatrix()
        glTranslatef(x, y, 0f)
        glScalef(scale, scale, 1f)
        glTranslatef(-x, -y, 0f)
        runnable.run()
        glPopMatrix()
    }

    /**
     * Sets the alpha test limit.
     *
     * @param limit Alpha limit (0.0 to 100.0)
     */
    @JvmStatic
    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL_GREATER, limit * 0.01f)
    }

    /**
     * Checks if mouse is hovering over a rectangular area.
     *
     * @param x Area X position
     * @param y Area Y position
     * @param width Area width
     * @param height Area height
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @return True if mouse is hovering
     */
    @JvmStatic
    fun isHovering(x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
    }

    /**
     * Interpolates between two double values.
     *
     * @param current Current value
     * @param old Old value
     * @param scale Interpolation factor
     * @return Interpolated value
     */
    @JvmStatic
    fun interpolate(current: Double, old: Double, scale: Double): Double {
        return old + (current - old) * scale
    }

    /**
     * Connects two points with a line (for particles).
     *
     * @param xOne First point X
     * @param yOne First point Y
     * @param xTwo Second point X
     * @param yTwo Second point Y
     */
    @JvmStatic
    fun connectPoints(xOne: Float, yOne: Float, xTwo: Float, yTwo: Float) {
        glPushMatrix()
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(0.1f)
        glBegin(GL_LINES)
        glVertex2f(xOne, yOne)
        glVertex2f(xTwo, yTwo)
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    /**
     * Draws a line between two points.
     *
     * @param x Start X
     * @param y Start Y
     * @param x1 End X
     * @param y1 End Y
     * @param width Line width
     */
    @JvmStatic
    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    /**
     * Enables smooth line rendering.
     *
     * @param width Line width
     */
    @JvmStatic
    fun enableSmoothLine(width: Float) {
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glLineWidth(width)
    }

    /**
     * Disables smooth line rendering.
     */
    @JvmStatic
    fun disableSmoothLine() {
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glDisable(GL_BLEND)
    }
}
