package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side

@ElementInfo(name = "Targets", single = true)
class Targets : Element(0.0,0.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    override fun drawElement(partialTicks: Float): Border? {
        return null
    }
}