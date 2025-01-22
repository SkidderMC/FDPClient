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
import net.ccbluex.liquidbounce.config.FloatValue
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Float Element - YZY GUI
 * @author opZywl
 */
class FloatElement(
    private val element: ModuleElement,
    private val setting: FloatValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private var dragging = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val min = setting.minimum
        val max = setting.maximum
        val value = setting.get()

        if (dragging) {
            var newValue = round((mouseX - x) * (max - min) / width + min)
            newValue = max(min, min(max, newValue))
            setting.set(newValue)
        }

        val percentage = width / (max - min)
        val barWidth = percentage * value - percentage * min
        val categoryColor = parent.category.color

        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width = barWidth, height = height.toFloat(),
            color = categoryColor
        )

        RenderUtils.yzyRectangle(
            (x + barWidth - 3), y.toFloat(),
            3.0f, height.toFloat(),
            categoryColor.darker()
        )

        val font = FDPClient.customFontManager["lato-bold-15"]

        font?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        val roundedValue = round(setting.get()).toInt().toString()

        font?.drawString(
            roundedValue,
            (x + width - 3 - font.getWidth(roundedValue)).toFloat(),
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