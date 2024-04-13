/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.abs

class LiquidButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    private var buttonWidthOffset = button.width / 2F

    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        buttonWidthOffset = interpolateValue(if(button.hovered) { button.width / 15F } else { 0F }, buttonWidthOffset, 0.6F)
        RenderUtils.drawRect(button.xPosition.toFloat() + buttonWidthOffset, button.yPosition.toFloat(), button.xPosition.toFloat() + button.width.toFloat() - buttonWidthOffset, button.yPosition.toFloat() + button.height.toFloat(), Color(0, 0, 0, (120F + (buttonWidthOffset * 4F)).toInt().coerceIn(0, 200)).rgb)
        GlStateManager.resetColor()
    }

    private fun interpolateValue(targetValue: Float, currentValue: Float, speed: Float): Float {
        if (currentValue == targetValue) { return currentValue }
        val delta = 0.1F.coerceAtLeast(abs(targetValue - currentValue) * (speed / 5F).coerceIn(0.0F, 1.0F))
        return when {
            targetValue > currentValue -> currentValue + delta
            else -> currentValue - delta
        }
    }
}