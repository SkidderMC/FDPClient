package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class KeyBindMgr : GuiScreen() {
    private val keys=ArrayList<KeyInfo>()

    init {
        //line1
        keys.add(KeyInfo(12F,12F,27F,32F,Keyboard.KEY_L/*IDK*/,"`"))
        keys.add(KeyInfo(12F+32F*1,12F,27F,32F,Keyboard.KEY_1,"1"))
        keys.add(KeyInfo(12F+32F*2,12F,27F,32F,Keyboard.KEY_2,"2"))
        keys.add(KeyInfo(12F+32F*3,12F,27F,32F,Keyboard.KEY_3,"3"))
        keys.add(KeyInfo(12F+32F*4,12F,27F,32F,Keyboard.KEY_4,"4"))
        keys.add(KeyInfo(12F+32F*5,12F,27F,32F,Keyboard.KEY_5,"5"))
        keys.add(KeyInfo(12F+32F*6,12F,27F,32F,Keyboard.KEY_6,"6"))
        keys.add(KeyInfo(12F+32F*7,12F,27F,32F,Keyboard.KEY_7,"7"))
        keys.add(KeyInfo(12F+32F*8,12F,27F,32F,Keyboard.KEY_8,"8"))
        keys.add(KeyInfo(12F+32F*9,12F,27F,32F,Keyboard.KEY_9,"9"))
        keys.add(KeyInfo(12F+32F*10,12F,27F,32F,Keyboard.KEY_0,"0"))
        keys.add(KeyInfo(12F+32F*11,12F,27F,32F,Keyboard.KEY_MINUS,"-"))
        keys.add(KeyInfo(12F+32F*12,12F,27F,32F,Keyboard.KEY_EQUALS,"="))
        keys.add(KeyInfo(12F+32F*13,12F,27F+32F,32F,Keyboard.KEY_BACKSLASH,"Backspace"))
        //line2
        keys.add(KeyInfo(12F,12F+37F*1,32F*1.5F-5F,32F,Keyboard.KEY_TAB,"Tab"))
        keys.add(KeyInfo(12F+32F*1.5F,12F+37F*1,27F,32F,Keyboard.KEY_Q,"Q"))
        keys.add(KeyInfo(12F+32F*2.5F,12F+37F*1,27F,32F,Keyboard.KEY_W,"W"))
        keys.add(KeyInfo(12F+32F*3.5F,12F+37F*1,27F,32F,Keyboard.KEY_E,"E"))
        keys.add(KeyInfo(12F+32F*4.5F,12F+37F*1,27F,32F,Keyboard.KEY_R,"R"))
        keys.add(KeyInfo(12F+32F*5.5F,12F+37F*1,27F,32F,Keyboard.KEY_T,"T"))
        keys.add(KeyInfo(12F+32F*6.5F,12F+37F*1,27F,32F,Keyboard.KEY_Y,"Y"))
        keys.add(KeyInfo(12F+32F*7.5F,12F+37F*1,27F,32F,Keyboard.KEY_U,"U"))
        keys.add(KeyInfo(12F+32F*8.5F,12F+37F*1,27F,32F,Keyboard.KEY_I,"I"))
        keys.add(KeyInfo(12F+32F*9.5F,12F+37F*1,27F,32F,Keyboard.KEY_O,"O"))
        keys.add(KeyInfo(12F+32F*10.5F,12F+37F*1,27F,32F,Keyboard.KEY_P,"P"))
        keys.add(KeyInfo(12F+32F*11.5F,12F+37F*1,27F,32F,Keyboard.KEY_L/*IDK*/,"["))
        keys.add(KeyInfo(12F+32F*12.5F,12F+37F*1,27F,32F,Keyboard.KEY_L/*IDK*/,"]"))
        keys.add(KeyInfo(12F+32F*13.5F,12F+37F*1,32F*1.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"\\"))
        //line3
        keys.add(KeyInfo(12F,12F+37F*2,32F*2F-5F,32F,Keyboard.KEY_TAB,"Caps Lock"))
        keys.add(KeyInfo(12F+32F*2F,12F+37F*2,27F,32F,Keyboard.KEY_A,"A"))
        keys.add(KeyInfo(12F+32F*3F,12F+37F*2,27F,32F,Keyboard.KEY_S,"S"))
        keys.add(KeyInfo(12F+32F*4F,12F+37F*2,27F,32F,Keyboard.KEY_D,"D"))
        keys.add(KeyInfo(12F+32F*5F,12F+37F*2,27F,32F,Keyboard.KEY_F,"F"))
        keys.add(KeyInfo(12F+32F*6F,12F+37F*2,27F,32F,Keyboard.KEY_G,"G"))
        keys.add(KeyInfo(12F+32F*7F,12F+37F*2,27F,32F,Keyboard.KEY_H,"H"))
        keys.add(KeyInfo(12F+32F*8F,12F+37F*2,27F,32F,Keyboard.KEY_J,"J"))
        keys.add(KeyInfo(12F+32F*9F,12F+37F*2,27F,32F,Keyboard.KEY_K,"K"))
        keys.add(KeyInfo(12F+32F*10F,12F+37F*2,27F,32F,Keyboard.KEY_L,"L"))
        keys.add(KeyInfo(12F+32F*11F,12F+37F*2,27F,32F,Keyboard.KEY_L/*IDK*/,";"))
        keys.add(KeyInfo(12F+32F*12F,12F+37F*2,27F,32F,Keyboard.KEY_L/*IDK*/,"'"))
        keys.add(KeyInfo(12F+32F*13F,12F+37F*2,27F+32F,32F,Keyboard.KEY_L/*IDK*/,"Enter"))
        //line4
        keys.add(KeyInfo(12F,12F+37F*3,32F*2.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Shift","LShift"))
        keys.add(KeyInfo(12F+32F*2.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"Z"))
        keys.add(KeyInfo(12F+32F*3.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"X"))
        keys.add(KeyInfo(12F+32F*4.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"C"))
        keys.add(KeyInfo(12F+32F*5.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"V"))
        keys.add(KeyInfo(12F+32F*6.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"B"))
        keys.add(KeyInfo(12F+32F*7.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"N"))
        keys.add(KeyInfo(12F+32F*8.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"M"))
        keys.add(KeyInfo(12F+32F*9.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,","))
        keys.add(KeyInfo(12F+32F*10.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"."))
        keys.add(KeyInfo(12F+32F*11.5F,12F+37F*3,27F,32F,Keyboard.KEY_Z,"/"))
        keys.add(KeyInfo(12F+32F*12.5F,12F+37F*3,32F*2.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Shift","RShift"))
        //line5
        keys.add(KeyInfo(12F,12F+37F*4,32F*1.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Ctrl","LCtrl"))
        keys.add(KeyInfo(12F+32F*1.5F,12F+37F*4,32F*1.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Alt","LAlt"))
        keys.add(KeyInfo(12F+32F*3F,12F+37F*4,32*8F-5F,32F,Keyboard.KEY_SPACE," ","Space"))
        keys.add(KeyInfo(12F+32F*11F,12F+37F*4,32F*1.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Alt","RAlt"))
        keys.add(KeyInfo(12F+32F*12.5F,12F+37F*4,27F,32F,Keyboard.KEY_HOME,"\u0000","Home"))
        keys.add(KeyInfo(12F+32F*13.5F,12F+37F*4,32F*1.5F-5F,32F,Keyboard.KEY_L/*IDK*/,"Ctrl","RCtrl"))
    }

    override fun initGui() {
        //use async because this may a bit slow
        Thread {
            for (key in keys) {
                key.update()
            }
        }.start()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val baseHeight=205
        val baseWidth=500
        val mcWidth=((width*0.8f)-(width*0.2f)).toInt()

        GL11.glPushMatrix()
        RenderUtils.drawText("KeyBind Manager", Fonts.fontBold40, (width * 0.21).toInt(), (height * 0.2).toInt(), 2f, Color.WHITE.rgb, false)
        GL11.glTranslatef(width*0.2f,height * 0.2f + Fonts.fontBold40.height * 2.3f,0F)
        GL11.glScalef(mcWidth/baseWidth.toFloat(),mcWidth/baseWidth.toFloat(),mcWidth/baseWidth.toFloat())

        RenderUtils.drawRect(0F,0F,baseWidth.toFloat(),baseHeight.toFloat(),Color.WHITE.rgb)

        for(key in keys){
            key.render()
        }

        GL11.glPopMatrix()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}