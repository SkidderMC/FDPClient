package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.RenderHelper
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Inventory")
class Inventory : Element(200.0,100.0,1F, Side(Side.Horizontal.RIGHT,Side.Vertical.UP)) {
    private val bgRedValue = IntegerValue("BGRed", 255, 0, 255)
    private val bgGreenValue = IntegerValue("BGGreen", 255, 0, 255)
    private val bgBlueValue = IntegerValue("BGBlue", 255, 0, 255)
    private val bgAlphaValue = IntegerValue("BGAlpha", 255, 0, 255)
    private val bdRedValue = IntegerValue("BDRed", 255, 0, 255)
    private val bdGreenValue = IntegerValue("BDGreen", 255, 0, 255)
    private val bdBlueValue = IntegerValue("BDBlue", 255, 0, 255)
    private val bdRainbow = BoolValue("BDRainbow",false)

    override fun drawElement(partialTicks: Float): Border {
        val borderColor=if(bdRainbow.get()){ColorUtils.rainbow()}else{Color(bdRedValue.get(),bdGreenValue.get(),bdBlueValue.get())}

        RenderHelper.enableGUIStandardItemLighting()
        renderInv(9,17,6,6)
        renderInv(18,26,6,24)
        renderInv(27,35,6,42)
        RenderHelper.disableStandardItemLighting()

        return Border(0F,0F,174F,66F)
    }

    private fun renderInv(slot: Int,endSlot: Int,x: Int,y: Int){
        var xOffset=x
        for(i in slot..endSlot){
            xOffset+=18
            val stack=mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue

            GL11.glPushMatrix()
            mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset-18, y)
            GL11.glPopMatrix()
        }
    }
}