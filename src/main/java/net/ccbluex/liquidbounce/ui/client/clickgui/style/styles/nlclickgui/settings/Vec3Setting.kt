/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.Vec3Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.input.Keyboard
import java.awt.Color

class Vec3Setting(setting: Vec3Value, moduleRender: NlModule) : Downward<Vec3Value>(setting, moduleRender) {

    private val labels = arrayOf("X", "Y", "Z")
    private val fieldWidth = 26f
    private val gap = 2f

    private var editingComponent = -1
    private var buffer = ""

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

        for (i in 0 until 3) {
            val fx = mainx + 170 + x + i * (fieldWidth + gap)
            val editing = editingComponent == i
            RenderUtil.drawRoundedRect(
                fx,
                (mainy + modey + 54).toFloat(),
                fieldWidth,
                14f,
                2F,
                if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
                1F,
                if (editing) NeverloseGui.neverlosecolor.rgb else Color(13, 24, 35).rgb
            )

            val componentText = if (editing) buffer else String.format("%.1f", setting.value[i])
            Fonts.Nl_15.drawString(
                "${labels[i]}:$componentText",
                fx + 2f,
                (mainy + modey + 59).toFloat(),
                if (editing) NeverloseGui.neverlosecolor.rgb else if (gui.light) Color(95, 95, 95).rgb else -1
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        val gui = NeverloseGui.getInstance()
        commit()
        editingComponent = -1
        for (i in 0 until 3) {
            val fx = gui.x + 170 + x + i * (fieldWidth + gap)
            if (RenderUtil.isHovering(fx, (gui.y + (y + getScrollY()).toInt() + 54).toFloat(), fieldWidth, 14f, mouseX, mouseY)) {
                editingComponent = i
                buffer = String.format("%.1f", setting.value[i])
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (editingComponent < 0) return
        when (keyCode) {
            Keyboard.KEY_RETURN, Keyboard.KEY_ESCAPE -> {
                if (keyCode == Keyboard.KEY_RETURN) commit()
                editingComponent = -1
            }
            Keyboard.KEY_BACK -> if (buffer.isNotEmpty()) buffer = buffer.dropLast(1)
            else -> if (typedChar.isDigit() || typedChar == '.' || typedChar == '-') buffer += typedChar
        }
    }

    private fun commit() {
        if (editingComponent < 0) return
        val parsed = buffer.toDoubleOrNull() ?: return
        when (editingComponent) {
            0 -> setting.x = parsed
            1 -> setting.y = parsed
            2 -> setting.z = parsed
        }
    }
}
