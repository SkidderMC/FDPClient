package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * @author liulihaocai
 * FDPClient
 */
class KeyInfo(val posX: Float,val posY: Float,val width: Float,val height: Float,val key: Int,val keyName:String,val keyDisplayName:String) : MinecraftInstance() {
    constructor(posX: Float,posY: Float,width: Float,height: Float,key: Int,keyName:String)
            : this(posX, posY, width, height, key, keyName, keyName)

    private val keyColor=Color(240,240,240).rgb
    private val shadowColor=Color(210,210,210).rgb
    private val unusedColor=Color(200,200,200).rgb
    private val usedColor=Color(0,0,0).rgb
    private val baseTabHeight=150
    private val baseTabWidth=100
    private val direction=posY>=100

    private var modules=ArrayList<Module>()
    private var hasKeyBind=false
    private var stroll=0
    private var maxStroll=0

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

    fun renderTab(){
        GL11.glPushMatrix()

        GL11.glTranslatef((posX+width*0.5F)-baseTabWidth*0.5F,if(direction){posY-baseTabHeight}else{posY+height},0F)
        RenderUtils.drawRect(0F,0F,baseTabWidth.toFloat(),baseTabHeight.toFloat(),Color.WHITE.rgb)

        //render modules
        val fontHeight=10F-Fonts.font40.height*0.5F
        var yOffset=(12F+Fonts.font40.height+10F)-stroll
        for(module in modules){
            if(yOffset>0&&(yOffset-20)<100) {
                GL11.glPushMatrix()
                GL11.glTranslatef(0F, yOffset, 0F)

                Fonts.font40.drawString(module.name, 12F, fontHeight, Color.DARK_GRAY.rgb, false)
                Fonts.font40.drawString(
                    "-", baseTabWidth - 12F - Fonts.font40.getStringWidth("-"), fontHeight, Color.RED.rgb, false
                )

                GL11.glPopMatrix()
            }
            yOffset+=20
        }

        //覆盖多出来的部分
        RenderUtils.drawRect(0F,0F,baseTabWidth.toFloat(),12F+Fonts.font40.height+10F,Color.WHITE.rgb)
        RenderUtils.drawRect(0F,baseTabHeight-22F-Fonts.font40.height,baseTabWidth.toFloat(),baseTabHeight.toFloat(),Color.WHITE.rgb)
        Fonts.font40.drawString("$keyDisplayName Key",12F,12F,Color.BLACK.rgb,false)
        Fonts.font40.drawString("+ Add",baseTabWidth-12F-Fonts.font40.getStringWidth("+ Add")
            ,baseTabHeight-12F-Fonts.font40.height,Color(0,191,255).rgb/*sky blue*/,false)

        GL11.glPopMatrix()
    }

    fun stroll(mouseX: Float, mouseY: Float,wheel: Int){
        val scaledMouseX=mouseX-((posX+width*0.5F)-baseTabWidth*0.5F)
        val scaledMouseY=mouseY-(if(direction){posY-baseTabHeight}else{posY+height})
        if(scaledMouseX<0||scaledMouseY<0||scaledMouseX>baseTabWidth||scaledMouseY>baseTabHeight)
            return

        val afterStroll=stroll-(wheel/40)
        if(afterStroll>0&&afterStroll<(maxStroll-150)){
            stroll=afterStroll
        }
    }

    fun update(){
        modules=LiquidBounce.moduleManager.getKeyBind(key) as ArrayList<Module>
        hasKeyBind=modules.size>0
        stroll=0
        maxStroll=modules.size*30
    }

    fun click(mouseX: Float, mouseY: Float){
        val keyBindMgr=LiquidBounce.keyBindMgr

        if(keyBindMgr.nowDisplayKey==null) {
            keyBindMgr.nowDisplayKey = this
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
        }
    }
}