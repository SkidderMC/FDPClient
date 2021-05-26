package net.ccbluex.liquidbounce.ui.click.rainbow

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.click.utils.ClickGuiUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import org.lwjgl.opengl.GL11
import java.awt.Color

class ClickList(val category: ModuleCategory, var x: Int, var y: Int, var modules: List<Module>) : MinecraftInstance() {
    var toggle=false

    fun render(){
        GL11.glPushMatrix()
        GL11.glTranslatef(x.toFloat(),y.toFloat(),0F)

        ClickGuiUtils.rainbowRect(0.0,0.0,100.0,20.0,255)
        Fonts.fontBold40.drawString(category.displayName,6F,10F-(Fonts.fontBold40.height*0.5F), Color.WHITE.rgb)

        if(toggle) {
            ClickGuiUtils.borderRect(0.0, 20.0, 100.0, 300.0)
        }

        GL11.glPopMatrix()
    }
}