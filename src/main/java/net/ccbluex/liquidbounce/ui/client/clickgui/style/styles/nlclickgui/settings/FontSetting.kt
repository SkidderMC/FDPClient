/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class FontSetting(setting: FontValue, moduleRender: NlModule) : Downward<FontValue>(setting, moduleRender) {

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val fontY = (y + getScrollY()).toInt()

        val (label, labelTruncated) = abbreviate(setting.name)
        val labelX = (mainx + 100 + x).toFloat()
        val labelY = (mainy + fontY + 57).toFloat()

        Fonts.Nl.Nl_16.Nl_16.drawString(
            label,
            labelX,
            labelY,
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        if (labelTruncated && RenderUtil.isHovering(labelX, labelY - 3f, Fonts.Nl.Nl_16.Nl_16.stringWidth(label).toFloat(), 12f, mouseX, mouseY)) {
            drawTooltip(setting.name, mouseX, mouseY)
        }

        val (display, truncated) = abbreviate(setting.displayName)
        val rectWidth = calculateRectWidth(display)

        val rectX = mainx + 140 + x
        val rectY = mainy + fontY + 54

        RenderUtil.drawRoundedRect(
            rectX,
            rectY.toFloat(),
            rectWidth.toFloat(),
            14f,
            2f,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString("<", (rectX + 4).toFloat(), (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)
        Fonts.Nl_15.drawString(">", (rectX + rectWidth - 9).toFloat(), (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)

        Fonts.Nl_15.drawCenteredString(
            display,
            (rectX + rectWidth / 2f),
            (rectY + 5).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        if (truncated && RenderUtil.isHovering(rectX.toFloat(), rectY.toFloat(), rectWidth.toFloat(), 14f, mouseX, mouseY)) {
            drawTooltip(setting.displayName, mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val rectX = gui.x + 140 + x
        val rectY = gui.y + (y + getScrollY()).toInt() + 54
        val display = abbreviate(setting.displayName).first

        val rectWidth = calculateRectWidth(display)

        if (mouseButton == 0 && RenderUtil.isHovering(rectX, rectY.toFloat(), rectWidth.toFloat(), 14f, mouseX, mouseY)) {
            val relativeX = mouseX - rectX
            when {
                relativeX < 20 -> setting.previous()
                relativeX > rectWidth - 20 -> setting.next()
                else -> setting.next()
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    private fun abbreviate(value: String): Pair<String, Boolean> {
        return if (value.length > 10) {
            value.substring(0, 10) + "..." to true
        } else {
            value to false
        }
    }

    private fun calculateRectWidth(display: String): Int {
        return max(100, min(140, Fonts.Nl_15.stringWidth(display) + 20))
    }

    private fun drawTooltip(text: String, mouseX: Int, mouseY: Int) {
        val width = Fonts.Nl_15.stringWidth(text) + 6
        val height = Fonts.Nl_15.height + 4
        val renderX = (mouseX + 6).toFloat()
        val renderY = (mouseY - height - 2).toFloat()

        RenderUtil.drawRoundedRect(renderX, renderY, width.toFloat(), height.toFloat(), 2f, Color(0, 5, 19).rgb, 1f, Color(13, 24, 35).rgb)
        Fonts.Nl_15.drawString(text, renderX + 3f, renderY + 2f, Color.WHITE.rgb)
    }
}