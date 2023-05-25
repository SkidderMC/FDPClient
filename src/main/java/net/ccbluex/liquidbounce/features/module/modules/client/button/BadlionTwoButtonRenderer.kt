/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.util.ResourceLocation

class BadlionTwoButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        val hoveredimg = ResourceLocation("fdpclient/ui/buttons/bhover.png")
        val elseimg = ResourceLocation("fdpclient/ui/buttons/bbutton.png")
        if(button.hovered) { RenderUtils.drawImage(hoveredimg, button.xPosition, button.yPosition, button.width, button.height) } else { RenderUtils.drawImage(elseimg, button.xPosition, button.yPosition, button.width, button.height) }
    }
}