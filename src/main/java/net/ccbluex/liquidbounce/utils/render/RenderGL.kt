/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * OpenGL state management utilities
 *
 * @author Zywl
 */
object RenderGL {

    private val glCapMap = mutableMapOf<Int, Boolean>()

    fun resetCaps() = glCapMap.clear()

    fun enableGlCap(cap: Int) {
        glCapMap[cap]?.let {
            if (!it) {
                glEnable(cap)
                glCapMap[cap] = true
            }
        } ?: run {
            glEnable(cap)
            glCapMap[cap] = true
        }
    }

    fun enableGlCap(vararg caps: Int) = caps.forEach(::enableGlCap)

    fun disableGlCap(cap: Int) {
        glCapMap[cap]?.let {
            if (it) {
                glDisable(cap)
                glCapMap[cap] = false
            }
        } ?: run {
            glDisable(cap)
            glCapMap[cap] = false
        }
    }

    fun disableGlCap(vararg caps: Int) = caps.forEach(::disableGlCap)

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F)
    }

    fun glColor(color: Color) = glColor(color.red, color.green, color.blue, color.alpha)

    fun glColor(hex: Int) {
        glColor(
            hex shr 16 and 0xFF,
            hex shr 8 and 0xFF,
            hex and 0xFF,
            hex shr 24 and 0xFF
        )
    }

    inline fun <T> scissorBox(x: Int, y: Int, width: Int, height: Int, action: () -> T): T {
        glEnable(GL_SCISSOR_TEST)
        glScissor(x, y, width, height)
        val result = action()
        glDisable(GL_SCISSOR_TEST)
        return result
    }

    fun setupBlend() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun startSmooth() {
        glEnable(GL_POLYGON_SMOOTH)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_POINT_SMOOTH)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST)
    }

    fun endSmooth() {
        glDisable(GL_POLYGON_SMOOTH)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_POINT_SMOOTH)
    }

    fun start2D() {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
    }

    fun stop2D() {
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }
}
