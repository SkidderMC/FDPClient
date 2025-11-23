package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

object GLUtil {
    fun render(mode: Int, render: Runnable) {
        GL11.glBegin(mode)
        render.run()
        GL11.glEnd()
    }

    fun setup2DRendering(f: Runnable) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        f.run()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GlStateManager.disableBlend()
    }

    fun rotate(x: Float, y: Float, rotate: Float, f: Runnable) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.rotate(rotate, 0f, 0f, -1f)
        GlStateManager.translate(-x, -y, 0f)
        f.run()
        GlStateManager.popMatrix()
    }
}