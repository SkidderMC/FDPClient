package net.ccbluex.liquidbounce.ui.click

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen

class ClickGui : GuiScreen() {
    // rainbow bg
//    override fun drawDefaultBackground() {
//        val alpha=1F
//        RenderUtils.drawColorRect(0.0,0.0,this.width.toDouble(),this.height.toDouble()
//            , ColorUtils.rainbow(0,alpha)
//            , ColorUtils.rainbow(1000000L*this.height,alpha)
//            , ColorUtils.rainbow(1000000L*(this.height*width),alpha)
//            , ColorUtils.rainbow(1000000L*this.width,alpha))
//    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}