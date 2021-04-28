package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.other.PopUI
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * @author liulihaocai
 * FDPClient
 */
class KeySelectUI(val info: KeyInfo) : PopUI("Select mod to bind") {
    private var str=""
    private var modules=LiquidBounce.moduleManager.modules.toList()
    private var stroll=0
    private var maxStroll=0F
    private val singleHeight=4F+Fonts.font35.height

    override fun render(){
        val height=4F+Fonts.font40.height+Fonts.font35.height+0.5F

        //modules
        var yOffset=height+2F
        for(module in modules){
            if(yOffset>(height-singleHeight)&&(yOffset-singleHeight)<130) {
                GL11.glPushMatrix()
                GL11.glTranslatef(0F, yOffset, 0F)

                val name=if(str.isNotEmpty()){module.name.replace(str,"§l$str§r")}else{module.name}
                Fonts.font35.drawString(name,0F,singleHeight*0.5F,Color.BLACK.rgb,false)

                GL11.glPopMatrix()
            }
            yOffset+=singleHeight
        }
        RenderUtils.drawRect(0F,4F+Fonts.font40.height,baseWidth.toFloat(),height,Color.WHITE.rgb)
        RenderUtils.drawRect(0F,baseHeight-singleHeight,baseWidth.toFloat(),baseHeight.toFloat(),Color.WHITE.rgb)

        //search bar
        Fonts.font35.drawString(str.ifEmpty { "Search..." },8F,8F+Fonts.font40.height+4F, Color.LIGHT_GRAY.rgb,false)
        RenderUtils.drawRect(0F,14F+Fonts.font40.height+Fonts.font35.height,baseWidth-16F
            ,height,Color.LIGHT_GRAY.rgb)
    }

    override fun key(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_BACK) {
            if (str.isNotEmpty())
                str=str.substring(0, str.length - 1)

            update()
            return
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
            str += typedChar

        update()
    }

    override fun stroll(mouseX: Float, mouseY: Float, wheel: Int) {
        val afterStroll=stroll-(wheel/20)
        if(afterStroll>0&&afterStroll<(maxStroll-100)){
            stroll=afterStroll
        }
    }

    override fun click(mouseX: Float, mouseY: Float) {

    }

    override fun close() {
        LiquidBounce.keyBindMgr.popUI=null
    }

    private fun update(){
        modules=LiquidBounce.moduleManager.modules.filter { it.name.contains(str,ignoreCase = true) }
        stroll=0
        maxStroll=modules.size*singleHeight
    }
}