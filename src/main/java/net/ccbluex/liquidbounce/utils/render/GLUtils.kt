/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.ClientUtils.mc
import net.ccbluex.liquidbounce.utils.extensions.renderBoundingBox
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BlurShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.CircleShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RoundRectShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.SRoundRectShader
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20
import java.awt.Color

object GLUtils {

    /**
     * Read [Information](https://en.wikipedia.org/wiki/Delta_timing)
     */
    @JvmField
    var deltaTime = 0

    /**
     * Draws a Rectangle
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param color Rectangle color
     */
    @JvmStatic
    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glColor(color)

        glBegin(GL_QUADS)

        glVertex2f(x2, y)
        glVertex2f(x, y)
        glVertex2f(x, y2)
        glVertex2f(x2, y2)

        glEnd()

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
    }

    /**
     * Draws a rectangular border
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param width Border width
     *
     * @param color Border color
     */
    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(width)
        glColor(color)

        glBegin(GL_LINE_LOOP)

        glVertex2f(x2, y)
        glVertex2f(x, y)
        glVertex2f(x, y2)
        glVertex2f(x2, y2)

        glEnd()

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
    }

    /**
     * Draws a rectangle with a border
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param width Border width
     *
     * @param color Border color
     * @param color2 Rectangle color
     */
    @JvmStatic
    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int, color2: Int = color) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color)
    }

    /**
     * Draws a rounded rectangle
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param radius Corner radius
     *
     * @param color Rectangle color
     */
    @JvmStatic
    fun drawRoundedRect(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Color) {
        require(radius in 0f..1f) { "Rectangle radius should be between 0 and 1" }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        RoundRectShader.startShader()

        GL20.glUniform4f(RoundRectShader.getUniform("color"), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        GL20.glUniform2f(RoundRectShader.getUniform("size"), x2 - x, y2 - y)
        GL20.glUniform1f(RoundRectShader.getUniform("radius"), radius)

        drawQuads(x, y, x2, y2)

        RoundRectShader.stopShader()

        glDisable(GL_BLEND)
    }

    /**
     * Draws a rounded rectangle with a separate radius for each corner
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param tr Top right corner radius
     * @param br Bottom right corner radius
     * @param tl Top left corner radius
     * @param br Bottom left corner radius
     *
     * @param color Rectangle color
     */
    @Suppress("unused")
    // @TODO Use this in clickgui
    fun drawSeparateRoundedRect(x: Float, y: Float, x2: Float, y2: Float, tr: Float, br: Float, tl: Float, bl: Float, color: Color) {
        require(tr in 0f..1f && br in 0f..1f && tl in 0f..1f && br in 0f..1f) { "Rectangle radii should be between 0 and 1" }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        SRoundRectShader.startShader()

        GL20.glUniform4f(SRoundRectShader.getUniform("color"), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        GL20.glUniform2f(SRoundRectShader.getUniform("size"), x2 - x, y2 - y)
        GL20.glUniform4f(SRoundRectShader.getUniform("radius"), tr, br, tl, bl)

        drawQuads(x, y, x2, y2)

        SRoundRectShader.stopShader()

        glDisable(GL_BLEND)
    }

    @JvmStatic
    fun drawQuads(x: Float, y: Float, x2: Float, y2: Float) {
        glBegin(GL_QUADS)

        glTexCoord2f(0f, 0f)
        glVertex2f(x, y)
        glTexCoord2f(0f, 1f)
        glVertex2f(x, y2)
        glTexCoord2f(1f, 1f)
        glVertex2f(x2, y2)
        glTexCoord2f(1f, 0f)
        glVertex2f(x2, y)

        glEnd()
    }

    /**
     * Draws a circle
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     *
     * @param color Circle color
     */
    @JvmStatic
    fun drawCircle(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        CircleShader.startShader()

        GL20.glUniform4f(CircleShader.getUniform("color"), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        GL20.glUniform2f(CircleShader.getUniform("size"), x2 - x, y2 - y)

        drawQuads(x, y, x2, y2)

        CircleShader.stopShader()

        glDisable(GL_BLEND)
    }

    /**
     * Creates a Scissor box
     *
     * [Documentation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glScissor.xml)
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
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
     * Draws an image
     *
     * @param image Image location
     * @param x X position
     * @param y Y position
     * @param width Image width
     * @param height Image height
     */
    @JvmStatic
    fun drawImage(image: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())

        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
    }

    /**
     * Draws a normal line
     *
     * @param x Start X position
     * @param y Start Y position
     * @param x2 End X position
     * @param y2 End Y position
     * @param width Line width
     */
    fun drawLine(x: Double, y: Double, x2: Double, y2: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)

        glVertex2d(x, y)
        glVertex2d(x2, y2)

        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    /**
     * draws a cuboid outline around [boundingBox]
     */
    fun drawOutlinedBB(boundingBox: AxisAlignedBB) {
        glBegin(GL_LINE_STRIP)

        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)

        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)

        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        
        glEnd()
    }

    /**
     * draws a filled cuboid around [boundingBox]
     */
    fun drawFilledBB(boundingBox: AxisAlignedBB) {
        glBegin(GL_QUADS)

        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
        glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
        glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        
        glEnd()
    }

    /**
     * Interpolates position into position for rendering
     *
     * @param old Previous position
     * @param current Current position
     */
    fun interpolate(old: Double, current: Double): Double {
        return old + (current - old) * mc.timer.renderPartialTicks.toDouble()
    }

    fun glColor(red: Float, green: Int, blue: Int, alpha: Int = 255) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    @JvmStatic
    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    private fun glColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    fun blur(radius: Int, f: () -> Unit) {
        StencilUtils.initStencil(mc.framebuffer)
        StencilUtils.writeToStencil()
        f()
        StencilUtils.readFromStencil()

        glPushMatrix()
        mc.entityRenderer.setupOverlayRendering()
        BlurShader.blur(radius)
        glPopMatrix()

        StencilUtils.uninitStencil()
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val bb = entity.renderBoundingBox

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)

        drawFilledBB(
            AxisAlignedBB(
                bb.minX,
                bb.maxY + 0.2,
                bb.minZ,
                bb.maxX,
                bb.maxY + 0.26,
                bb.maxZ
            )
        )

        GlStateManager.resetColor()
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
    }
}