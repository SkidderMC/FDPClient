/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.MultiSelectValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

/**
 * Multi Select Element - YZY GUI
 */
class MultiSelectElement(
    private val element: ModuleElement,
    private val setting: MultiSelectValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private val rowHeight = 12

    fun getActualHeight(): Int = rowHeight * (setting.choices.size + 1)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return

        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), getActualHeight().toFloat(),
            Color(26, 26, 26)
        )

        font.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (rowHeight / 4.0f) + 0.5f,
            -1
        )

        setting.choices.forEachIndexed { index, choice ->
            val rowY = y + rowHeight * (index + 1)
            val selected = setting.isSelected(choice)
            val rowColor = if (selected) {
                Color(70, 70, 70)
            } else {
                Color(34, 34, 34)
            }

            RenderUtils.yzyRectangle(
                (x + 2).toFloat(), rowY.toFloat(),
                (width - 4).toFloat(), (rowHeight - 1).toFloat(),
                rowColor
            )

            font.drawString(
                (if (selected) "[x] " else "[ ] ") + choice,
                (x + 4).toFloat(),
                rowY + (rowHeight / 4.0f) + 0.5f,
                if (selected) Color.WHITE.rgb else Color(0xD2D2D2).rgb
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (button != 0) return
        setting.choices.forEachIndexed { index, choice ->
            val rowY = y + rowHeight * (index + 1)
            if (mouseX in (x + 2)..(x + width - 2) && mouseY in rowY..(rowY + rowHeight - 1)) {
                setting.toggle(choice)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}
}
