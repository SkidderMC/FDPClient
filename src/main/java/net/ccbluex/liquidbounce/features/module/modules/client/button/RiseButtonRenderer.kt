package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import org.lwjgl.opengl.GL11
import java.awt.Color

class RiseButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        val startX = button.xPosition.toFloat()
        val endX = button.xPosition + button.width.toFloat()
        val endY = button.yPosition + button.height.toFloat()
        RenderUtils.drawRect(startX, button.yPosition.toFloat(), endX, endY,
            (if(button.hovered) { Color(60, 60, 60, 150) } else { Color(31, 31, 31, 150) }).rgb)
        if (button.enabled) {
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(2848)
            GL11.glShadeModel(7425)
            for (i in button.xPosition..button.xPosition + button.width step button.width / 20) {
                RenderUtils.quickDrawGradientSideways(startX.toDouble(), endY - 1.0, endX.toDouble(), endY.toDouble(),
                    ColorUtils.hslRainbow(startX.toInt(), indexOffset = 70).rgb, ColorUtils.hslRainbow(endX.toInt(), indexOffset = 70).rgb)
            }
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glDisable(2848)
            GL11.glShadeModel(7424)
            GL11.glColor4f(1f, 1f, 1f, 1f)
        }
    }
}