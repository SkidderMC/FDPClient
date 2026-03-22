/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

abstract class NumericSliderElement(
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : PanelElement(parent, x, y, width, height) {

    private var dragging = false

    protected abstract val label: String
    protected abstract val minimum: Float
    protected abstract val maximum: Float
    protected abstract val currentValue: Float

    protected abstract fun setValue(value: Float)
    protected abstract fun formatValue(value: Float): String
    protected open fun normalizeDraggedValue(value: Float) = value

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (dragging) {
            var newValue = (mouseX - x) * (maximum - minimum) / width + minimum
            newValue = max(minimum, min(maximum, normalizeDraggedValue(newValue)))
            setValue(newValue)
        }

        val percentage = width / (maximum - minimum)
        val barWidth = percentage * currentValue - percentage * minimum
        val categoryColor = parent.category.color

        RenderUtils.yzyRectangle(
            x.toFloat(),
            y.toFloat(),
            width = barWidth,
            height = height.toFloat(),
            color = categoryColor,
        )

        RenderUtils.yzyRectangle(
            x + barWidth - 3,
            y.toFloat(),
            3.0f,
            height.toFloat(),
            categoryColor.darker(),
        )

        val font = FDPClient.customFontManager["lato-bold-15"]

        font?.drawString(
            label,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1,
        )

        val renderedValue = formatValue(currentValue)
        font?.drawString(
            renderedValue,
            (x + width - 3 - font.getWidth(renderedValue)).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb,
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
