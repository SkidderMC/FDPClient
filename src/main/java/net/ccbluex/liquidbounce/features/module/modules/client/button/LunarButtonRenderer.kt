/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class LunarButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    var fade = 40F
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        fade = alphaFader(if(button.hovered) { 70F } else {40F}, fade, 0.2F )
        drawRoundedRect(button.xPosition.toFloat(), button.yPosition.toFloat(), button.xPosition + button.width.toFloat(), button.yPosition + button.height.toFloat(), 4f, Color(255, 255, 255, fade.toInt()).rgb)
        drawRoundedOutline(button.xPosition.toFloat(), button.yPosition.toFloat(), button.xPosition + button.width.toFloat(), button.yPosition + button.height.toFloat(), 4f, 2f, Color(255, 255, 255, 80).rgb)
        drawRoundedOutline(button.xPosition.toFloat() -1F , button.yPosition.toFloat() -1F, button.xPosition + button.width.toFloat()+1F, button.yPosition + button.height.toFloat()+1F, 4.5f, 1.5f, Color(0, 0, 0, 50).rgb)
        GlStateManager.resetColor()

    }

    fun drawRoundedRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, colour: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(colour)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_POLYGON)
        var i = 0
        while (i <= 90) {
            glVertex2d(x + radius + sin(i * Math.PI / 180.0) * radius * -1.0, y + radius + cos(i * Math.PI / 180.0) * radius * -1.0)
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(x + radius + sin(i * Math.PI / 180.0) * radius * -1.0, y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0)
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y + radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
    }

    private fun drawRoundedOutline(x: Float, y: Float, x1: Float, y1: Float, radius: Float, lineWidth: Float, colour: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(colour)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(lineWidth)
        glBegin(GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(x + radius + sin(i * Math.PI / 180.0) * radius * -1.0, y + radius + cos(i * Math.PI / 180.0) * radius * -1.0)
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(x + radius + sin(i * Math.PI / 180.0) * radius * -1.0, y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0)
            i += 3
        }
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        i = 90
        while (i <= 180) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y + radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glLineWidth(1f)
    }

    private fun setColour(colour: Int) {
        val a = (colour shr 24 and 0xFF) / 255.0f
        val r = (colour shr 16 and 0xFF) / 255.0f
        val g = (colour shr 8 and 0xFF) / 255.0f
        val b = (colour and 0xFF) / 255.0f
        glColor4f(r, g, b, a)
    }

    private fun alphaFader(target: Float, current: Float, speed: Float): Float {
        if (current == target) { return current }
        val factor = 0.1F.coerceAtLeast(abs(target - current) * (speed / 5F).coerceIn(0.0F, 1.0F))
        return when {
            target > current -> 255F.coerceAtMost(current + factor)
            else -> 0F.coerceAtLeast(current - factor)
        }
    }

}
