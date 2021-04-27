package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class KeyInfo(val posX: Float,val posY: Float,val width: Float,val height: Float,val key: Int,val keyName:String,val keyDisplayName:String) {
    constructor(posX: Float,posY: Float,width: Float,height: Float,key: Int,keyName:String)
            : this(posX, posY, width, height, key, keyName, keyName)

    private val keyColor=Color(240,240,240).rgb
    private val shadowColor=Color(210,210,210).rgb
    private val unusedColor=Color(200,200,200).rgb
    private val usedColor=Color(0,0,0).rgb

    private var modules=ArrayList<Module>()
    private var hasKeyBind=false

    init {

    }

    fun render(){
        GL11.glPushMatrix()
        GL11.glTranslatef(posX,posY,0F)

        RenderUtils.drawRect(0F,0F,width,height,keyColor)
        RenderUtils.drawRect(0F,height*0.9F,width,height,shadowColor)
        (if(hasKeyBind){Fonts.fontBold40}else{Fonts.font40})
            .drawCenteredString(keyName,width*0.5F,height*0.9F*0.5F-(Fonts.font35.FONT_HEIGHT*0.5F)+3F
            ,if(hasKeyBind){usedColor}else{unusedColor},false)

        GL11.glPopMatrix()
    }

    fun update(){
        modules=LiquidBounce.moduleManager.getKeyBind(key) as ArrayList<Module>
        hasKeyBind=modules.size>0
    }
}