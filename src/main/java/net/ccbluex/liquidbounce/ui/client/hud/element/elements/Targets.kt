package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.minecraft.entity.EntityLivingBase

// TODO: make it at v1.2.1
@ElementInfo(name = "Targets", single = true)
class Targets : Element(0.0,0.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    private val prevTarget:EntityLivingBase?=null

    override fun drawElement(partialTicks: Float): Border? {

//        prevTarget?:return Border(10F,10F,10F,10F)
        return null
    }
}