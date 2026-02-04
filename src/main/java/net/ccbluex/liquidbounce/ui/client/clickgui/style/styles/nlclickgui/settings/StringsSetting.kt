/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.opengl.GL11
import java.awt.Color
import net.minecraft.client.Minecraft

class StringsSetting(setting: ListValue, moduleRender: NlModule) : Downward<ListValue>(setting, moduleRender) {
    private var length = 3.0
    private var anim = 5.0

    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = NeverloseGui.getInstance().x
        val mainy = NeverloseGui.getInstance().y
        val modey = (y + getScrollY()).toInt()

        val (label, labelTruncated) = abbreviate(setting.name)
        val labelX = (mainx + 100 + x).toFloat()
        val labelY = (mainy + modey + 57).toFloat()

        Fonts.Nl_16.drawString(
            label,
            labelX,
            labelY,
            if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
        )

        if (labelTruncated && RenderUtil.isHovering(labelX, labelY - 3f, Fonts.Nl_16.stringWidth(label).toFloat(), 12f, mouseX, mouseY)) {
            drawTooltip(setting.name, mouseX, mouseY)
        }

        RenderUtil.drawRoundedRect(
            (mainx + 170 + x).toFloat(),
            (mainy + modey + 54).toFloat(),
            80f,
            14f,
            2F,
            if (NeverloseGui.getInstance().light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1F,
            Color(13, 24, 35).rgb
        )

        val (displayValue, truncatedCurrent) = abbreviate(setting.get())
        var pendingTooltip: String? = null

        Fonts.Nl_16.drawString(
            displayValue,
            (mainx + 173 + x).toFloat(),
            (mainy + modey + 59).toFloat(),
            if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
        )

        if (truncatedCurrent && RenderUtil.isHovering((mainx + 170 + x).toFloat(), (mainy + modey + 54).toFloat(), 80f, 14f, mouseX, mouseY)) {
            pendingTooltip = setting.get()
        }

        val valFps = Minecraft.getDebugFPS() / 8.3
        if (setting.openList && length > -3) {
            length -= 3 / valFps
        } else if (!setting.openList && length < 3) {
            length += 3 / valFps
        }
        if (setting.openList && anim < 8) {
            anim += 3 / valFps
        } else if (!setting.openList && anim > 5) {
            anim -= 3 / valFps
        }

        RenderUtil.drawArrow(
            (mainx + 240 + x).toDouble(),
            (mainy + modey + 55 + anim).toFloat().toDouble(),
            2,
            if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else Color(200, 200, 200).rgb,
            length
        )

        if (setting.openList) {
            GL11.glTranslatef(0f, 0f, 2f)

            RenderUtil.drawRoundedRect(
                (mainx + 170 + x).toFloat(),
                (mainy + modey + 68).toFloat(),
                80f,
                setting.values.size * 12f,
                2F,
                if (NeverloseGui.getInstance().light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
                1F,
                Color(13, 24, 35).rgb
            )

            for (option in setting.values) {
                val optionIndex = getIndex(option)
                val (optionDisplay, optionTruncated) = abbreviate(option)
                Fonts.Nl_15.drawString(
                    optionDisplay,
                    (mainx + 173 + x).toFloat(),
                    (mainy + modey + 59 + 12 + optionIndex * 12).toFloat(),
                    if (option.equals(setting.get(), true)) NeverloseGui.neverlosecolor.rgb else if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
                )

                if (optionTruncated && RenderUtil.isHovering((NeverloseGui.getInstance().x + 170 + x).toFloat(), (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 59 + 12 + optionIndex * 12).toFloat(), 80f, 12f, mouseX, mouseY)) {
                    pendingTooltip = option
                }
            }
            GL11.glTranslatef(0f, 0f, -2f)
        }

        pendingTooltip?.let { drawTooltip(it, mouseX, mouseY) }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 1 && RenderUtil.isHovering((NeverloseGui.getInstance().x + 170 + x).toFloat(), (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 54).toFloat(), 80f, 14f, mouseX, mouseY)) {
            setting.openList = !setting.openList
        }
        if (mouseButton == 0) {
            if (setting.openList && mouseX >= NeverloseGui.getInstance().x + 170 + x && mouseX <= NeverloseGui.getInstance().x + 170 + x + 80) {
                for (i in setting.values.indices) {
                    val v = NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 59 + 12 + i * 12
                    if (mouseY >= v && mouseY <= v + 12) {
                        setting.set(setting.values[i], true)
                    }
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    private fun getIndex(option: String): Int {
        for (i in setting.values.indices) {
            if (setting.values[i].equals(option, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun abbreviate(value: String): Pair<String, Boolean> {
        return if (value.length > 10) {
            value.substring(0, 10) + "..." to true
        } else {
            value to false
        }
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