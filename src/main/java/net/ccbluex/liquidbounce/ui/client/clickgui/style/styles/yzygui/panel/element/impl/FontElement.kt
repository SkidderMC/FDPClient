/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import java.awt.Color

class FontElement(
    private val element: ModuleElement,
    private val setting: FontValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : PanelElement(parent, x, y, width, height) {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font = FDPClient.customFontManager["lato-bold-15"]
        val value = setting.displayName
        font?.drawString(setting.name, (x + 1).toFloat(), y + (height / 4.0f) + 0.5f, -1)
        font?.drawString(
            value,
            (x + width - (font.getWidth(value)) - 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb,
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            if (button == 0) setting.next() else if (button == 1) setting.previous()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}
}
