/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color
import kotlin.math.sqrt

class RGBRoundedButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        var index = 1
        RenderUtils.drawRoundedCornerRect(button.xPosition.toFloat(), button.yPosition.toFloat(), button.xPosition + button.width.toFloat(), button.yPosition + button.height.toFloat(), sqrt((button.width * button.height).toDouble()).toFloat() * 0.1f, (if(button.hovered) { ColorUtils.hslRainbow( index + 1, indexOffset = 100 * 1) } else { Color(50, 50, 53, 250) }).rgb)
        RenderUtils.drawRoundedCornerRect(button.xPosition.toFloat() + 1F , button.yPosition.toFloat() + 1, button.xPosition + button.width.toFloat() - 1, button.yPosition + button.height.toFloat() - 1, sqrt((button.width * button.height).toDouble()).toFloat() * 0.1f, Color(50,50,53).rgb)
    }
}
