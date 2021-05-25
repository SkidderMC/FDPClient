package net.ccbluex.liquidbounce.ui.click.rainbow

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.click.utils.ClickGuiUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.awt.Color

class ClickList(val category: ModuleCategory, var x: Int, var y: Int, var modules: List<Module>) : MinecraftInstance() {
    fun render(){
        ClickGuiUtils.rainbowRect(x.toDouble(),y.toDouble(),x+100.0,y+20.0,255)
        Fonts.fontBold40.drawString(category.displayName,x+6F,y+10F-(Fonts.fontBold40.height*0.5F), Color.WHITE.rgb)

        ClickGuiUtils.borderRect(x.toDouble(),y+20.0,x+100.0,y+300.0)
    }
}