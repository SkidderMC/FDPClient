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
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Basic 2D/3D shape rendering primitives
 *
 * @author Zywl
 */
object RenderPrimitives {

    private var batchDepth = 0

    /**
     * Groups primitive-only drawing into one Tessellator submission. The block must
     * not invoke font, texture or other Tessellator rendering while the batch is open.
     */
    fun batch(block: () -> Unit) {
        val outermost = batchDepth == 0
        if (outermost) beginBatch()
        batchDepth++

        try {
            block()
        } finally {
            batchDepth--
            if (outermost) endBatch()
        }
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        if (batchDepth > 0) appendRect(x, y, x2, y2, color)
        else batch { appendRect(x, y, x2, y2, color) }
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) = drawRect(x, y, x2, y2, color.rgb)

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        Gui.drawRect(x, y, x2, y2, color)
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        batch {
            drawRect(x, y, x2, y2, rectColor)
            drawBorder(x, y, x2, y2, width, borderColor)
        }
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        batch {
            drawRect(x - width, y, x, y2, color)
            drawRect(x2, y, x2 + width, y2, color)
            drawRect(x - width, y - width, x2 + width, y, color)
            drawRect(x - width, y2, x2 + width, y2 + width, color)
        }
    }

    fun drawRoundedRect(
        x: Float, y: Float, x2: Float, y2: Float,
        round: Float, color: Int
    ) {
        if (batchDepth > 0) appendRoundedRect(x, y, x2, y2, round, color)
        else batch { appendRoundedRect(x, y, x2, y2, round, color) }
    }

    private fun beginBatch() {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.color(1f, 1f, 1f, 1f)
        Tessellator.getInstance().worldRenderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR)
    }

    private fun endBatch() {
        Tessellator.getInstance().draw()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun appendRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        val left = minOf(x, x2)
        val top = minOf(y, y2)
        val right = maxOf(x, x2)
        val bottom = maxOf(y, y2)

        vertex(left, top, color)
        vertex(left, bottom, color)
        vertex(right, bottom, color)
        vertex(left, top, color)
        vertex(right, bottom, color)
        vertex(right, top, color)
    }

    private fun appendRoundedRect(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Int) {
        val left = minOf(x, x2)
        val top = minOf(y, y2)
        val right = maxOf(x, x2)
        val bottom = maxOf(y, y2)
        val safeRadius = radius.coerceIn(0f, min(right - left, bottom - top) / 2f)

        if (safeRadius <= 0f) {
            appendRect(left, top, right, bottom, color)
            return
        }

        appendRect(left + safeRadius, top, right - safeRadius, bottom, color)
        appendRect(left, top + safeRadius, left + safeRadius, bottom - safeRadius, color)
        appendRect(right - safeRadius, top + safeRadius, right, bottom - safeRadius, color)

        val segments = ceil(safeRadius / 2f).toInt().coerceIn(4, 16)
        appendCorner(left + safeRadius, top + safeRadius, safeRadius, PI, segments, color)
        appendCorner(right - safeRadius, top + safeRadius, safeRadius, PI * 1.5, segments, color)
        appendCorner(right - safeRadius, bottom - safeRadius, safeRadius, 0.0, segments, color)
        appendCorner(left + safeRadius, bottom - safeRadius, safeRadius, PI * 0.5, segments, color)
    }

    private fun appendCorner(
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Double,
        segments: Int,
        color: Int
    ) {
        val angleStep = (PI / 2.0) / segments
        for (segment in 0 until segments) {
            val angle = startAngle + angleStep * segment
            val nextAngle = angle + angleStep
            vertex(centerX, centerY, color)
            vertex(centerX + radius * cos(angle).toFloat(), centerY + radius * sin(angle).toFloat(), color)
            vertex(centerX + radius * cos(nextAngle).toFloat(), centerY + radius * sin(nextAngle).toFloat(), color)
        }
    }

    private fun vertex(x: Float, y: Float, color: Int) {
        Tessellator.getInstance().worldRenderer
            .pos(x.toDouble(), y.toDouble(), 0.0)
            .color(color shr 16 and 0xFF, color shr 8 and 0xFF, color and 0xFF, color ushr 24)
            .endVertex()
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
