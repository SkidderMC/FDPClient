/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.CurveValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.core.CurveEditor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class CurveSetting(setting: CurveValue, moduleRender: NlModule) : Downward<CurveValue>(setting, moduleRender) {

    private var draggingIndex = -1

    private val graphWidth = 120f
    private val graphHeight = 38f

    override fun rowHeight(): Int = 60

    private fun graphX(): Float = (NeverloseGui.getInstance().x + 100 + x)
    private fun graphY(): Float = (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 60).toFloat()

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val modey = (y + getScrollY()).toInt()

        Fonts.Nl_16.drawString(
            setting.name,
            (gui.x + 100 + x).toFloat(),
            (gui.y + modey + 50).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        val gx = graphX()
        val gy = graphY()
        val count = setting.pointCount

        RenderUtil.drawRoundedRect(
            gx, gy, graphWidth, graphHeight, 2f,
            if (gui.light) Color(235, 235, 235).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        if (draggingIndex in 0 until count) {
            setting.setPoint(draggingIndex, CurveEditor.valueFromY(mouseY.toFloat(), gy, graphHeight))
        }

        val c = NeverloseGui.neverlosecolor
        GlStateManager.color(c.red / 255f, c.green / 255f, c.blue / 255f, 1f)
        for (i in 0 until count - 1) {
            RenderUtils.drawLine(
                CurveEditor.pointX(i, count, gx, graphWidth).toDouble(),
                CurveEditor.pointY(setting.getPoint(i), gy, graphHeight).toDouble(),
                CurveEditor.pointX(i + 1, count, gx, graphWidth).toDouble(),
                CurveEditor.pointY(setting.getPoint(i + 1), gy, graphHeight).toDouble(),
                1.5f
            )
        }
        RenderUtils.resetColor()

        for (i in 0 until count) {
            RoundedUtil.drawCircle(
                CurveEditor.pointX(i, count, gx, graphWidth),
                CurveEditor.pointY(setting.getPoint(i), gy, graphHeight),
                if (i == draggingIndex) 3.2f else 2.4f,
                c
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        val gx = graphX()
        val gy = graphY()
        if (RenderUtil.isHovering(gx, gy - 2f, graphWidth, graphHeight + 4f, mouseX, mouseY)) {
            draggingIndex = CurveEditor.nearestIndex(mouseX.toFloat(), setting.pointCount, gx, graphWidth)
            setting.setPoint(draggingIndex, CurveEditor.valueFromY(mouseY.toFloat(), gy, graphHeight))
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingIndex = -1
    }
}
