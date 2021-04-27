package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class KeyInfo(val posX: Float,val posY: Float,val width: Float,val height: Float,val key: Int,val keyName:String) {
    private val keyColor=Color(210,210,210)
    private val shadowColor=Color.LIGHT_GRAY

    private var modules=ArrayList<Module>()
    private var hasKeyBind=false

    init {

    }

    fun render(mcWidth:Int,mcHeight:Int){
        GL11.glPushMatrix()
        GL11.glTranslatef(posX*mcWidth,posY*mcHeight,0F)
        val keyStopX=width*mcWidth
        val keyStopY=height*mcHeight

        RenderUtils.drawRect(0F,0F,keyStopX,keyStopY,keyColor.rgb)
        RenderUtils.drawRect(0F,keyStopY,keyStopX,keyStopY+mcHeight*0.02F,shadowColor.rgb)
        Fonts.font35.drawCenteredString(keyName,keyStopX*0.5F,keyStopY*0.5F-(Fonts.font35.FONT_HEIGHT*0.5F),Color.BLACK.rgb,false)

        GL11.glPopMatrix()
    }

    fun update(){
        modules=LiquidBounce.moduleManager.getKeyBind(key) as ArrayList<Module>
        hasKeyBind=modules.size>0
    }
}