package net.ccbluex.liquidbounce.ui.click

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class ClickGui : GuiScreen() {
    // arrow 🢒

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var x = 0
        while (x < (width+50)) {
            var y = 0
            while (y < (height+50)){
                rainbowRect(x.toDouble(),y.toDouble(),x+50.0,y+50.0,100)
                y += 50
            }
            x += 50
        }

        rainbowRect(100.0,100.0,200.0,120.0,255)
    }

    private fun rainbowRect(x1: Double, y1: Double, x2: Double, y2: Double, alpha: Int){
        RenderUtils.drawColorRect(x1,y1,x2,y2,getColor(x1,y1,alpha),getColor(x1,y2,alpha),getColor(x2,y2,alpha),getColor(x2,y1,alpha))
    }

    private fun getColor(x: Double, y: Double, alpha: Int):Color {
        return ColorUtils.rainbow(((x+1)*(y+1)).toLong()*10000L,alpha)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}