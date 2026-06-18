/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.input.Keyboard
import java.awt.Color

class KeyBindSetting(setting: KeyBindValue, moduleRender: NlModule) : Downward<KeyBindValue>(setting, moduleRender) {

    private var binding = false

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

        val keyText = if (binding) "..." else setting.keyName
        Fonts.Nl_15.drawCenteredString(
            keyText,
            (mainx + 210 + x).toFloat(),
            (mainy + modey + 59).toFloat(),
            if (binding) NeverloseGui.neverlosecolor.rgb else if (gui.light) Color(95, 95, 95).rgb else -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        if (mouseButton == 0 && RenderUtil.isHovering((gui.x + 170 + x).toFloat(), (gui.y + (y + getScrollY()).toInt() + 54).toFloat(), 80f, 14f, mouseX, mouseY)) {
            binding = !binding
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!binding) return
        if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
            setting.set(Keyboard.KEY_NONE)
        } else {
            setting.set(keyCode)
        }
        binding = false
    }
}
