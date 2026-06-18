/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Key Bind Element - YZY GUI
 */
class KeyBindElement(
    private val element: ModuleElement,
    private val setting: KeyBindValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private var binding = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return

        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            Color(26, 26, 26)
        )

        font.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        val keyText = if (binding) "..." else setting.keyName
        font.drawString(
            keyText,
            (x + width - font.getWidth(keyText) - 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            binding = !binding
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {
        if (!binding) return
        if (code == Keyboard.KEY_SPACE || code == Keyboard.KEY_ESCAPE || code == Keyboard.KEY_DELETE) {
            setting.set(Keyboard.KEY_NONE)
        } else {
            setting.set(code)
        }
        binding = false
    }
}
