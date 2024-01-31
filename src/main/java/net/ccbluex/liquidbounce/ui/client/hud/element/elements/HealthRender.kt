/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import java.awt.Color

@ElementInfo(name = "HealthRender")
class HealthRender(
    x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    override fun drawElement(): Border {
        val health = mc.thePlayer!!.health
        val maxhealth = mc.thePlayer!!.maxHealth

        RoundedUtil.drawRound(0f, 0f, 120f, 15f, 3F, Color(0, 0, 0, 80))

        RoundedUtil.drawRound(0f, 0f, (health / maxhealth) * 120f, 15f, 3F, Color(4, 255, 88, 80))

        return Border(0f, 0f, 120f, 15f)
    }
}