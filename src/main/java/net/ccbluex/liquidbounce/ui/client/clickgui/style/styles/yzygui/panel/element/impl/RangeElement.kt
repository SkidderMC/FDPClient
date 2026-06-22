/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.core.RangeController
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class RangeElement private constructor(
    private val label: String,
    private val minimum: Float,
    private val maximum: Float,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val getLow: () -> Float,
    private val getHigh: () -> Float,
    private val setLow: (Float) -> Unit,
    private val setHigh: (Float) -> Unit,
    private val isInt: Boolean,
    private val suffix: String,
) : PanelElement(parent, x, y, width, height) {

    constructor(setting: IntRangeValue, parent: Panel, x: Int, y: Int, width: Int, height: Int) : this(
        setting.name, setting.minimum.toFloat(), setting.maximum.toFloat(), parent, x, y, width, height,
        { setting.get().first.toFloat() }, { setting.get().last.toFloat() },
        { setting.setFirst(round(it).toInt()) }, { setting.setLast(round(it).toInt()) }, true, setting.suffix ?: ""
    )

    constructor(setting: FloatRangeValue, parent: Panel, x: Int, y: Int, width: Int, height: Int) : this(
        setting.name, setting.minimum, setting.maximum, parent, x, y, width, height,
        { setting.get().start }, { setting.get().endInclusive },
        { setting.setFirst(it) }, { setting.setLast(it) }, false, setting.suffix ?: ""
    )

    private var draggingLow = false
    private var draggingHigh = false

    private fun valueAt(mouseX: Int): Float {
        val raw = (mouseX - x) * (maximum - minimum) / width + minimum
        val clamped = max(minimum, min(maximum, raw))
        return if (isInt) round(clamped) else clamped
    }

    private fun pixelOf(value: Float): Float {
        if (maximum - minimum == 0f) return x.toFloat()
        return x + (value - minimum) * width / (maximum - minimum)
    }

    private fun format(value: Float): String =
        if (isInt) round(value).toInt().toString() else (round(value * 100f) / 100f).toString()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (draggingLow) setLow(min(valueAt(mouseX), getHigh()))
        if (draggingHigh) setHigh(max(valueAt(mouseX), getLow()))

        val low = getLow()
        val high = getHigh()
        val categoryColor = parent.category.color
        val lowPixel = pixelOf(low)
        val highPixel = pixelOf(high)

        RenderUtils.yzyRectangle(
            lowPixel,
            y.toFloat(),
            width = highPixel - lowPixel,
            height = height.toFloat(),
            color = categoryColor,
        )

        RenderUtils.yzyRectangle(lowPixel, y.toFloat(), 3.0f, height.toFloat(), categoryColor.darker())
        RenderUtils.yzyRectangle(highPixel - 3, y.toFloat(), 3.0f, height.toFloat(), categoryColor.darker())

        val font = FDPClient.customFontManager["lato-bold-15"]
        font?.drawString(label, (x + 1).toFloat(), y + (height / 4.0f) + 0.5f, -1)

        val renderedValue = format(low) + " - " + format(high) + if (suffix.isNotEmpty()) " $suffix" else ""
        font?.drawString(
            renderedValue,
            (x + width - 3 - font.getWidth(renderedValue)).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb,
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (!isHovering(mouseX, mouseY)) return
        val lowPixel = pixelOf(getLow())
        val highPixel = pixelOf(getHigh())
        if (RangeController.nearerThumb(mouseX.toDouble(), lowPixel.toDouble(), highPixel.toDouble()) == 0) {
            draggingLow = true
        } else {
            draggingHigh = true
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingLow = false
        draggingHigh = false
    }

    override fun keyTyped(character: Char, code: Int) {}
}
