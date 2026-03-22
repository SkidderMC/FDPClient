/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Basic 2D/3D shape rendering primitives
 *
 * @author Zywl
 */
object RenderPrimitives {

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        var xx = x
        var yy = y
        var xx2 = x2
        var yy2 = y2

        if (xx < xx2) {
            val temp = xx
            xx = xx2
            xx2 = temp
        }

        if (yy < yy2) {
            val temp = yy
            yy = yy2
            yy2 = temp
        }

        val f = (color shr 24 and 0xFF) / 255.0f
        val f1 = (color shr 16 and 0xFF) / 255.0f
        val f2 = (color shr 8 and 0xFF) / 255.0f
        val f3 = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.color(f1, f2, f3, f)

        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        worldrenderer.pos(xx.toDouble(), yy2.toDouble(), 0.0).endVertex()
        worldrenderer.pos(xx2.toDouble(), yy2.toDouble(), 0.0).endVertex()
        worldrenderer.pos(xx2.toDouble(), yy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(xx.toDouble(), yy.toDouble(), 0.0).endVertex()
        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) = drawRect(x, y, x2, y2, color.rgb)

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        Gui.drawRect(x, y, x2, y2, color)
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        drawRect(x - width, y, x, y2, color)
        drawRect(x2, y, x2 + width, y2, color)
        drawRect(x - width, y - width, x2 + width, y, color)
        drawRect(x - width, y2, x2 + width, y2 + width, color)
    }

    fun drawRoundedRect(
        x: Float, y: Float, x2: Float, y2: Float,
        round: Float, color: Int
    ) {
        drawRect(x + round, y, x2 - round, y2, color)
        drawRect(x, y + round, x + round, y2 - round, color)
        drawRect(x2 - round, y + round, x2, y2 - round, color)
        drawCircleCorner(x + round, y + round, round, 0, color)
        drawCircleCorner(x2 - round, y + round, round, 1, color)
        drawCircleCorner(x2 - round, y2 - round, round, 2, color)
        drawCircleCorner(x + round, y2 - round, round, 3, color)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var angle: Float
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        val a = (color shr 24 and 0xFF) / 255.0f

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.color(r, g, b, a)

        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            angle = (dAngle * i).toFloat()
            glVertex2f(x + radius * cos(angle), y + radius * sin(angle))
        }

        glEnd()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawCircleCorner(x: Float, y: Float, radius: Float, corner: Int, color: Int) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        val a = (color shr 24 and 0xFF) / 255.0f

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.color(r, g, b, a)

        glBegin(GL_TRIANGLE_FAN)

        val range = Math.PI / 2
        val start = range * corner

        for (i in 0..18) {
            val angle = start + (range / 18) * i
            glVertex2f(x + radius * cos(angle).toFloat(), y + radius * sin(angle).toFloat())
        }

        glEnd()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawGradientRect(
        left: Double, top: Double, right: Double, bottom: Double,
        startColor: Int, endColor: Int, zLevel: Float = 0f
    ) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f

        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.shadeModel(GL_SMOOTH)

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right, top, zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left, top, zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left, bottom, zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right, bottom, zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()

        GlStateManager.shadeModel(GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double, color: Int, width: Float = 1f) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        val a = (color shr 24 and 0xFF) / 255.0f

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.color(r, g, b, a)
        glLineWidth(width)

        glBegin(GL_LINES)
        glVertex2d(x1, y1)
        glVertex2d(x2, y2)
        glEnd()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
    }
}
