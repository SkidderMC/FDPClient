package net.ccbluex.liquidbounce.ui.click.utils

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

object ClickGuiUtils {
    // for rainbow clickgui
    fun rainbowRect(x1: Double, y1: Double, x2: Double, y2: Double, alpha: Int){
        RenderUtils.drawColorRect(x1,y1,x2,y2,getColor(x1,y1,alpha),getColor(x1,y2,alpha),getColor(x2,y2,alpha),getColor(x2,y1,alpha))
    }

    fun borderRect(x1: Double, y1: Double, x2: Double, y2: Double){
        RenderUtils.drawRect(x1,y1,x2,y2, Color(60,60,60).rgb)
    }

    fun getColor(x: Double, y: Double, alpha: Int): Color {
        return ColorUtils.rainbow((x+1).toLong()*5000000L,alpha)
    }
}