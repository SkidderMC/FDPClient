/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Block Element - YZY GUI
 * @author opZywl
 */
class BlockElement(
    private val element: ModuleElement,
    private val setting: BlockValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private var dragging = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val min = setting.minimum.toFloat()
        val max = setting.maximum.toFloat()
        val value = setting.get().toFloat()

        if (dragging && mouseX >= x && mouseX <= x + width) {
            var newValue = ((mouseX - x).toFloat() / width * (max - min) + min)
            newValue = max(min, min(max, newValue))
            setting.set(newValue.toInt())
        }

        val percentage = (value - min) / (max - min)
        val barWidth = width * percentage
        val categoryColor = parent.category.color

        // Draw background
        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            Color(26, 26, 26)
        )

        // Draw progress bar
        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            barWidth, height.toFloat(),
            categoryColor
        )

        RenderUtils.yzyRectangle(
            (x + barWidth - 2).toFloat(), y.toFloat(),
            4.0f, height.toFloat(),
            categoryColor.brighter()
        )

        val font = FDPClient.customFontManager["lato-bold-15"]
        val blockName = BlockUtils.getBlockName(setting.get())
        val displayText = "$blockName (${setting.get()})"

        font?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        font?.drawString(
            displayText,
            (x + width - 3 - font.getWidth(displayText)).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            dragging = true
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = false
    }

    override fun keyTyped(character: Char, code: Int) {}
}