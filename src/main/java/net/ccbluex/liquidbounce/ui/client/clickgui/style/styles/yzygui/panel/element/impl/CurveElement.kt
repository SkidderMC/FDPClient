/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.CurveValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.core.CurveEditor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * Curve Element - YZY GUI
 *
 * Draws the [CurveValue] control points across a graph box and lets each point be dragged
 * vertically (height 0..1). Shares its geometry with every other style via [CurveEditor].
 */
class CurveElement(
    private val element: ModuleElement,
    private val setting: CurveValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private var draggingIndex = -1

    private val graphX get() = (x + 2).toFloat()
    private val graphY get() = (y + 12).toFloat()
    private val graphW get() = (width - 4).toFloat()
    private val graphH get() = (height - 14).toFloat()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font = FDPClient.customFontManager["lato-bold-15"]
        val categoryColor = parent.category.color

        font?.drawString(setting.name, (x + 1).toFloat(), y + 3f, -1)

        val gx = graphX
        val gy = graphY
        val gw = graphW
        val gh = graphH
        val count = setting.pointCount

        RenderPrimitives.drawRect(gx, gy, gx + gw, gy + gh, Color(20, 20, 20).rgb)
        RenderUtils.drawBorder(gx, gy, gx + gw, gy + gh, 1f, Color(60, 60, 60).rgb)

        if (draggingIndex in 0 until count) {
            setting.setPoint(draggingIndex, CurveEditor.valueFromY(mouseY.toFloat(), gy, gh))
        }

        GlStateManager.color(
            categoryColor.red / 255f, categoryColor.green / 255f, categoryColor.blue / 255f, 1f
        )
        for (i in 0 until count - 1) {
            RenderHelper.drawLine(
                CurveEditor.pointX(i, count, gx, gw).toDouble(),
                CurveEditor.pointY(setting.getPoint(i), gy, gh).toDouble(),
                CurveEditor.pointX(i + 1, count, gx, gw).toDouble(),
                CurveEditor.pointY(setting.getPoint(i + 1), gy, gh).toDouble(),
                1.5f
            )
        }
        RenderColor.resetColor()

        for (i in 0 until count) {
            val px = CurveEditor.pointX(i, count, gx, gw)
            val py = CurveEditor.pointY(setting.getPoint(i), gy, gh)
            val pointColor = if (i == draggingIndex) Color.WHITE else categoryColor.brighter()
            RenderUtils.drawFilledCircle(px.toInt(), py.toInt(), 2.4f, pointColor)
        }
    }

    private fun inGraph(mouseX: Int, mouseY: Int): Boolean {
        val gx = graphX
        val gy = graphY
        return mouseX >= gx && mouseX <= gx + graphW && mouseY >= gy - 2 && mouseY <= gy + graphH + 2
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0 && inGraph(mouseX, mouseY)) {
            draggingIndex = CurveEditor.nearestIndex(mouseX.toFloat(), setting.pointCount, graphX, graphW)
            setting.setPoint(draggingIndex, CurveEditor.valueFromY(mouseY.toFloat(), graphY, graphH))
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingIndex = -1
    }

    override fun keyTyped(character: Char, code: Int) {}
}
