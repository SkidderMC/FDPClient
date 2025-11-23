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

class FontSetting(setting: FontValue, moduleRender: NlModule) : Downward<FontValue>(setting, moduleRender) {

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val fontY = (y + getScrollY()).toInt()

        // Ajuste da Fonte para evitar erro de referência (padronizado com NlModule)
        // Se der erro, tente remover o ".Nl_16" extra
        Fonts.Nl.Nl_16.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x),
            (mainy + fontY + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        val display = setting.displayName
        val widthStr = Fonts.Nl_15.stringWidth(display)
        val rectWidth = max(100, widthStr + 20)

        val rectX = mainx + 170 + x
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

        Fonts.Nl_15.drawString("<", rectX + 4, (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)
        Fonts.Nl_15.drawString(">", rectX + rectWidth - 9, (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)

        Fonts.Nl_15.drawCenteredString(
            display,
            (rectX + rectWidth / 2f),
            (rectY + 5).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val rectX = gui.x + 170 + x
        val rectY = gui.y + (y + getScrollY()).toInt() + 54
        val display = setting.displayName

        val widthStr = Fonts.Nl_15.stringWidth(display)
        val rectWidth = max(100, widthStr + 20)

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
}