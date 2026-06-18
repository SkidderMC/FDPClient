/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.MultiSelectValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.opengl.GL11
import java.awt.Color

class MultiSelectSetting(setting: MultiSelectValue, moduleRender: NlModule) : Downward<MultiSelectValue>(setting, moduleRender) {
    private var length = 3.0
    private var anim = 5.0

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val modey = (y + getScrollY()).toInt()

        Fonts.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x).toFloat(),
            (mainy + modey + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        RenderUtil.drawRoundedRect(
            (mainx + 170 + x).toFloat(),
            (mainy + modey + 54).toFloat(),
            80f,
            14f,
            2F,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1F,
            Color(13, 24, 35).rgb
        )

        val summary = if (setting.get().isEmpty()) "None" else "${setting.get().size} selected"
        Fonts.Nl_16.drawString(
            summary,
            (mainx + 173 + x).toFloat(),
            (mainy + modey + 59).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        val valFps = Math.max(1.0, net.minecraft.client.Minecraft.getDebugFPS() / 8.3)
        if (setting.openList && length > -3) length -= 3 / valFps
        else if (!setting.openList && length < 3) length += 3 / valFps
        if (setting.openList && anim < 8) anim += 3 / valFps
        else if (!setting.openList && anim > 5) anim -= 3 / valFps

        RenderUtil.drawArrow(
            (mainx + 240 + x).toDouble(),
            (mainy + modey + 55 + anim).toFloat().toDouble(),
            2,
            if (gui.light) Color(95, 95, 95).rgb else Color(200, 200, 200).rgb,
            length
        )

        if (setting.openList) {
            GL11.glTranslatef(0f, 0f, 2f)
            RenderUtil.drawRoundedRect(
                (mainx + 170 + x).toFloat(),
                (mainy + modey + 68).toFloat(),
                80f,
                setting.choices.size * 12f,
                2F,
                if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
                1F,
                Color(13, 24, 35).rgb
            )

            setting.choices.forEachIndexed { index, choice ->
                val selected = setting.isSelected(choice)
                Fonts.Nl_15.drawString(
                    (if (selected) "[x] " else "[ ] ") + choice,
                    (mainx + 173 + x).toFloat(),
                    (mainy + modey + 59 + 12 + index * 12).toFloat(),
                    if (selected) NeverloseGui.neverlosecolor.rgb else if (gui.light) Color(95, 95, 95).rgb else -1
                )
            }
            GL11.glTranslatef(0f, 0f, -2f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        if (mouseButton == 1 && RenderUtil.isHovering((gui.x + 170 + x).toFloat(), (gui.y + (y + getScrollY()).toInt() + 54).toFloat(), 80f, 14f, mouseX, mouseY)) {
            setting.openList = !setting.openList
        }
        if (mouseButton == 0 && setting.openList && mouseX >= gui.x + 170 + x && mouseX <= gui.x + 170 + x + 80) {
            for (i in setting.choices.indices) {
                val v = gui.y + (y + getScrollY()).toInt() + 59 + 12 + i * 12
                if (mouseY >= v && mouseY <= v + 12) {
                    setting.toggle(setting.choices[i])
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
}
