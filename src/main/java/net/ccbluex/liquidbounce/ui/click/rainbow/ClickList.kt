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
    var toggleing=false
    var toggleTime=-1L
    var clickX=0
    var clickY=0

    fun render(){
        GL11.glPushMatrix()
        GL11.glTranslatef(x.toFloat(),y.toFloat(),0F)

        if(toggleing){
            
        }

        ClickGuiUtils.rainbowRect(0.0,0.0,100.0,20.0,255)
        Fonts.font40.drawString(category.displayName,6F,10F-(Fonts.font40.height*0.5F), Color.WHITE.rgb)

        if(toggle||toggleing) {
            ClickGuiUtils.borderRect(0.0, 20.0, 100.0, 300.0)
        }

        GL11.glPopMatrix()
    }

    fun inTitleArea(mouseX: Int, mouseY: Int):Boolean {
        if(!toggle&&(mouseX>x&&mouseX<(x+100)&&mouseY>y&&mouseY<(y+20))){
            return true
        }else if(toggle&&(mouseX>x&&mouseX<(x+100)&&mouseY>y&&mouseY<(y+320))){
            return true
        }
        return false
    }

    fun click(mouseX: Int, mouseY: Int){
        val translatedMouseX=mouseX-x
        val translatedMouseY=mouseY-y

        if(mouseX>0&&mouseY>0&&mouseX<100&&mouseY<20) {
            clickX = translatedMouseX
            clickY = translatedMouseY
            toggleTime = System.currentTimeMillis()
            toggleing = true
        }
    }
}