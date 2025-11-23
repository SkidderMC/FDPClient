/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class TextSetting(setting: TextValue, moduleRender: NlModule) : Downward<TextValue>(setting, moduleRender) {

    private var editing = false
    private var buffer: String = setting.get()

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val textY = (y + getScrollY()).toInt()

        Fonts.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x),
            (mainy + textY + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        val display = if (editing) buffer else setting.get()
        val stringWidth = Fonts.Nl_15.stringWidth(display) + 6

        RenderUtil.drawRoundedRect(
            (mainx + 170 + x),
            (mainy + textY + 54).toFloat(),
            max(80, stringWidth).toFloat(),
            14f,
            2f,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString(
            display,
            mainx + 174 + x,
            (mainy + textY + 59).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        if (!editing) {
            buffer = setting.get()
        } else {
            setting.set(buffer)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val boxX = (gui.x + 170 + x)
        val boxY = (gui.y + (y + getScrollY()).toInt() + 54).toFloat()

        val display = if (editing) buffer else setting.get()
        val stringWidth = Fonts.Nl_15.stringWidth(display) + 6
        val boxWidth = max(80, stringWidth).toFloat()

        if (mouseButton == 0) {
            editing = RenderUtil.isHovering(boxX, boxY, boxWidth, 14f, mouseX, mouseY)
            if (editing) {
                buffer = setting.get()
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!editing) return

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> editing = false
            Keyboard.KEY_BACK -> if (buffer.isNotEmpty()) buffer = buffer.substring(0, buffer.length - 1)
            Keyboard.KEY_RETURN -> {
                setting.set(buffer)
                editing = false
            }

            else -> {
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    buffer += typedChar
                }
            }
        }
    }
}