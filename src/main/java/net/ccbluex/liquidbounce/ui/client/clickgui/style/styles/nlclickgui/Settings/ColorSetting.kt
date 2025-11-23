package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color

class ColorSetting(setting: ColorValue, moduleRender: NlModule) : Downward<ColorValue>(setting, moduleRender) {
    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = NeverloseGui.getInstance().x
        val mainy = NeverloseGui.getInstance().y
        val colory = (y + getScrollY()).toInt()
        Fonts.Nl_16.drawString(setting.name, (mainx + 100 + x).toFloat(), (mainy + colory + 57).toFloat(), if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1)
        val color = setting.selectedColor()
        RoundedUtil.drawRound((mainx + 100 + x + 138).toFloat(), (mainy + colory + 52).toFloat(), 16f, 10f, 2f, color)
        RenderUtil.drawBorderedRect((mainx + 100 + x + 138).toFloat(), (mainy + colory + 52).toFloat(), (mainx + 100 + x + 154).toFloat(), (mainy + colory + 62).toFloat(), 1f, Color(0, 0, 0, 60).rgb, Color(0, 0, 0, 80).rgb)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 1 && RenderUtil.isHovering((NeverloseGui.getInstance().x + 100 + x + 138).toFloat(), (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 52).toFloat(), 16f, 10f, mouseX, mouseY)) {
            setting.rainbow = !setting.rainbow
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
}
