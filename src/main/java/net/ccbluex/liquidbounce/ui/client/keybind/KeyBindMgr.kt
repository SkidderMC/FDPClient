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
        keys.add(KeyInfo(0.1F,0.1F,0.1F,0.1F,Keyboard.KEY_R,"R"))
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

        val baseHeight=250
        val baseWidth=500
        val mcWidth=((width*0.8f)-(width*0.2f)).toInt()

        GL11.glPushMatrix()
        RenderUtils.drawText("KeyBind Manager", Fonts.fontBold40, (width * 0.21).toInt(), (height * 0.2).toInt(), 2f, Color.WHITE.rgb, false)
        GL11.glTranslatef(width*0.2f,height * 0.2f + Fonts.fontBold40.height * 2.3f,0F)
        GL11.glScalef(mcWidth/baseWidth.toFloat(),mcWidth/baseWidth.toFloat(),mcWidth/baseWidth.toFloat())

        RenderUtils.drawRect(0F,0F,baseWidth.toFloat(),baseHeight.toFloat(),Color.WHITE.rgb)

//        for(key in keys){
//            key.render(mcWidth,mcHeight)
//        }

        GL11.glPopMatrix()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}