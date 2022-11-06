package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.features.module.modules.client.UIEffects
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

class RGBButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        var index = 1
        var index2 = 10
        // RenderUtils.drawGradientSidewaysV(
        RenderUtils.drawGradientSidewaysH(button.xPosition.toDouble(), button.yPosition.toDouble(), button.xPosition + button.width.toDouble(), button.yPosition + button.height.toDouble(), (if(button.hovered) { ColorUtils.hslRainbow( index + 1, indexOffset = 100 * 1) } else { Color(0, 0, 0, 255) }).rgb, (if(button.hovered) { ColorUtils.hslRainbow( index2 + 1, indexOffset = 100 * 1)  } else { Color(0, 0, 0, 255) }).rgb)
        RenderUtils.drawRect(button.xPosition.toFloat() + 1F , button.yPosition.toFloat() + 1, button.xPosition + button.width.toFloat() - 1, button.yPosition + button.height.toFloat() - 1, Color(0,0,0).rgb)
        if (UIEffects.buttonShadowValue.equals(true)){ shadowRenderUtils.drawShadowWithCustomAlpha(button.xPosition.toFloat(), button.yPosition.toFloat(), button.width.toFloat(), button.height.toFloat(), 240f) }
    }
}