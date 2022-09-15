/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.utils

import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
class Particle(var color: Color, var distX: Float, var distY: Float, var radius: Float, var drawType: ShapeType = ShapeType.SOLID_CIRCLE) {
    var alpha = 1F
    var progress = 0.0
    var rotate = 0F
    fun render(x: Float, y: Float, fade: Boolean, speed: Float, fadeSpeed: Float, canRotate: Boolean = false) {
        if (progress >= 1.0) {
            progress = 1.0
            if (fade) alpha -= (fadeSpeed * 0.02F * RenderUtils.deltaTime)
            if (alpha < 0F) alpha = 0F
        } else
            progress += (speed * 0.025F * RenderUtils.deltaTime).toDouble()

        if (alpha <= 0F) return

        val reColored = Color(color.red / 255.0F, color.green / 255.0F, color.blue / 255.0F, alpha)
        val easeOut = EaseUtils.easeOutQuart(progress).toFloat()

        if (canRotate && drawType != ShapeType.SOLID_CIRCLE && drawType != ShapeType.CIRCLE) {
            rotate += 10F * (1F - easeOut)
            GL11.glPushMatrix()
            GL11.glTranslatef(x + distX * easeOut, y + distY * easeOut, 0F)
            GL11.glPushMatrix()
            GL11.glRotatef(rotate, 0F, 0F, 1F)
            drawType.performRendering(0F, 0F, radius, reColored)
            GL11.glPopMatrix()
            GL11.glPopMatrix()
        } else
            drawType.performRendering(x + distX * easeOut, y + distY * easeOut, radius, reColored)
    }
}

enum class ShapeType(val typeName: String) {
    SOLID_CIRCLE("c_solid") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawFilledCircle(x, y, rad, col)
        }
    },
    CIRCLE("c_outline") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawCircle(x, y, rad, 0.5F, 0, 360, col)
        }
    },
    SOLID_RECT("r_solid") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawRect(x - rad / 2F, y - rad / 2F, x + rad / 2F, y + rad / 2F, col.rgb)
        }
    },
    RECT("r_outline") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawBorder(x - rad / 2F, y - rad / 2F, x + rad / 2F, y + rad / 2F, 0.5F, col.rgb)
        }
    },
    SOLID_TRIANGLE("t_solid") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawTriAngle(x, y, rad, 3F, col, true)
        }
    },
    TRIANGLE("t_outline") {
        override fun performRendering(x: Float, y: Float, rad: Float, col: Color) {
            RenderUtils.drawTriAngle(x, y, rad, 3F, col, false)
        }
    };
    abstract fun performRendering(x: Float, y: Float, rad: Float, col: Color)
    
    companion object {
        fun getTypeFromName(name: String): ShapeType? {
            return values().find { it.typeName.equals(name, true) }
        }
    }
}