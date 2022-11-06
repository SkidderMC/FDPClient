/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeInOutQuad
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.features.module.modules.client.UIEffects
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

class FLineButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {

    private var animation = 0.0
    private var lastUpdate = System.currentTimeMillis()

    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / 500.0

        if (button.hovered) {
            if (animation < 1) {
                animation += pct
            }
            if (animation > 1) {
                animation = 1.0
            }
        } else {
            if (animation > 0) {
                animation -= pct
            }
            if (animation < 0) {
                animation = 0.0
            }
        }

        val percent = easeInOutQuad(animation)
        RenderUtils.drawRect(button.xPosition.toFloat(), button.yPosition.toFloat(), (button.xPosition + button.width).toFloat(), (button.yPosition + button.height).toFloat(), Color(31, 31, 31, 150).rgb)
        if (button.enabled) {
            val half = button.width / 2.0
            val center = button.xPosition + half
            RenderUtils.drawRect(center - percent * half, (button.yPosition + button.height - 1).toDouble(), center + percent * half, (button.yPosition + button.height).toDouble(), Color.WHITE.rgb)
        }

        lastUpdate = time

            if (UIEffects.buttonShadowValue.equals(true)){
            shadowRenderUtils.drawShadowWithCustomAlpha(button.xPosition.toFloat(), button.yPosition.toFloat(), button.width.toFloat(), button.height.toFloat(), 240f)
            }
    }
}