package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color
import kotlin.math.sqrt

class RoundedButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        RenderUtils.drawCircleRect(button.xPosition.toFloat(), button.yPosition.toFloat(),
            button.xPosition + button.width.toFloat(), button.yPosition + button.height.toFloat(),
            sqrt((button.width * button.height).toDouble()).toFloat() * 0.1f,
            (if(button.hovered) { Color(60, 60, 60, 150) } else { Color(31, 31, 31, 150) }).rgb)
    }
}