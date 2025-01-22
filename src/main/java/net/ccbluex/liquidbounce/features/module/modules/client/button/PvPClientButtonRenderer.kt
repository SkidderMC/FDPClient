/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import java.awt.Color

class PvPClientButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        if (!button.visible) { return }
        button(button.xPosition - 1, button.yPosition - 1, button.width + 2, 1, Color(140, 140, 140, 79).rgb) //t
        button(button.xPosition - 1, button.yPosition + button.height, button.width + 2, 1, Color(140, 140, 140, 79).rgb) //b
        button(button.xPosition - 1, button.yPosition, 1, button.height, Color(140, 140, 140, 79).rgb) //l
        button(button.xPosition + button.width, button.yPosition, 1, button.height, Color(140, 140, 140, 79).rgb) //r
        button(button.xPosition, button.yPosition, button.width, button.height, if (button.hovered) Color(0, 0, 0, 180).rgb else Color(0, 0, 0, 128).rgb)
   }

    private fun button(x: Int, y: Int, width: Int, height: Int, colour: Int){
        Gui.drawRect(x, y, x + width, y + height, colour)
    }
}