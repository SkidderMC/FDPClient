/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.config.ListValue
import java.awt.Color

/**
 * List Element - YZY GUI
 * @author opZywl
 */
class ListElement(
    private val element: ModuleElement,
    private val setting: ListValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font: FontRenderer? = FDPClient.customFontManager["lato-bold-15"]
        val value = setting.get()

        font?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        font?.drawString(
            value,
            (x + width - font.getWidth(value) - 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            cycle(button == 0)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}

    private fun cycle(next: Boolean) {
        val values = setting.values.toList()
        var index = values.indexOf(setting.get())

        index = if (next) {
            (index + 1) % values.size
        } else {
            (index - 1 + values.size) % values.size
        }

        setting.changeValue(values[index])
    }
}