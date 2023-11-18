/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.features.module.modules.client.UIEffects
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
            for (i in button.xPosition..button.xPosition + button.width step 1) {
                RenderUtils.quickDrawGradientSidewaysH(i.toDouble(), endY - 1.0, i + 1.0, endY.toDouble(),
                    ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(i + 1, indexOffset = 10).rgb)
            }
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glDisable(2848)
            GL11.glShadeModel(7424)
            GL11.glColor4f(1f, 1f, 1f, 1f)
            if (UIEffects.buttonShadowValue.equals(true)){
            shadowRenderUtils.drawShadowWithCustomAlpha(button.xPosition.toFloat(), button.yPosition.toFloat(), button.width.toFloat(), button.height.toFloat(), 240f)
            }
        }
    }
}