/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.util.MathHelper
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RangeSetting(
    setting: Value<*>,
    moduleRender: NlModule
) : Downward<Value<*>>(setting, moduleRender) {

    private var draggingLeft = false
    private var draggingRight = false

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val rangeY = (y + getScrollY()).toInt()

        val barX = (mainx + 170 + x).toFloat()
        val barY = (mainy + rangeY + 58).toFloat()

        val intRange = setting as? IntRangeValue
        val floatRange = setting as? FloatRangeValue

        val (minimum, maximum, currentStart, currentEnd) = when {
            intRange != null -> Quadruple(
                intRange.minimum.toDouble(),
                intRange.maximum.toDouble(),
                intRange.get().first.toDouble(),
                intRange.get().last.toDouble()
            )

            floatRange != null -> Quadruple(
                floatRange.minimum.toDouble(),
                floatRange.maximum.toDouble(),
                floatRange.get().start.toDouble(),
                floatRange.get().endInclusive.toDouble()
            )

            else -> return
        }

        val percentStart = ((currentStart - minimum) / (maximum - minimum)).coerceIn(0.0, 1.0)
        val percentEnd = ((currentEnd - minimum) / (maximum - minimum)).coerceIn(0.0, 1.0)

        val startX = barX + (60 * percentStart).toFloat()
        val endX = barX + (60 * percentEnd).toFloat()

        // Fonte Ajustada para o padrão
        Fonts.Nl.Nl_16.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x),
            (mainy + rangeY + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        RoundedUtil.drawRound(barX, barY, 60f, 2f, 2f, if (gui.light) Color(230, 230, 230) else Color(5, 22, 41))

        val fillStart = min(startX, endX)
        val fillWidth = abs(endX - startX)

        RoundedUtil.drawRound(fillStart, barY, max(2f, fillWidth), 2f, 2f, NeverloseGui.neverlosecolor)

        RoundedUtil.drawCircle(startX, barY - 2, 5.5f, NeverloseGui.neverlosecolor)
        RoundedUtil.drawCircle(endX, barY - 2, 5.5f, NeverloseGui.neverlosecolor)

        if (draggingLeft || draggingRight) {
            val percent = ((mouseX.toFloat() - barX) / 60f).coerceIn(0f, 1f)
            val newValue = minimum + (maximum - minimum) * percent

            intRange?.let {
                if (draggingLeft) it.setFirst(MathHelper.floor_double(newValue).coerceAtMost(it.get().last), true)
                if (draggingRight) it.setLast(MathHelper.floor_double(newValue).coerceAtLeast(it.get().first), true)
            }

            floatRange?.let {
                if (draggingLeft) it.setFirst(newValue.toFloat().coerceAtMost(it.get().endInclusive), true)
                if (draggingRight) it.setLast(newValue.toFloat().coerceAtLeast(it.get().start), true)
            }
        }

        val valueString = when {
            intRange != null -> "${intRange.get().first} - ${intRange.get().last}${intRange.suffix ?: ""}"
            floatRange != null -> "${"%.2f".format(floatRange.get().start)} - ${"%.2f".format(floatRange.get().endInclusive)}${floatRange.suffix ?: ""}"
            else -> ""
        }

        val stringWidth = Fonts.Nl_15.stringWidth(valueString) + 4

        RenderUtil.drawRoundedRect(
            (mainx + 235 + x).toFloat(),
            (mainy + rangeY + 55).toFloat(),
            stringWidth.toFloat(),
            9f,
            1f,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString(
            valueString,
            mainx + 237 + x,
            (mainy + rangeY + 58).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val barX = (gui.x + 170 + x).toFloat()
        val barY = (gui.y + (y + getScrollY()).toInt() + 58).toFloat()

        val intRange = setting as? IntRangeValue
        val floatRange = setting as? FloatRangeValue

        val percentStart: Double
        val percentEnd: Double
        when {
            intRange != null -> {
                percentStart = (intRange.get().first - intRange.minimum).toDouble() / (intRange.maximum - intRange.minimum)
                percentEnd = (intRange.get().last - intRange.minimum).toDouble() / (intRange.maximum - intRange.minimum)
            }

            floatRange != null -> {
                percentStart = ((floatRange.get().start - floatRange.minimum) / (floatRange.maximum - floatRange.minimum)).toDouble()
                percentEnd = ((floatRange.get().endInclusive - floatRange.minimum) / (floatRange.maximum - floatRange.minimum)).toDouble()
            }

            else -> return
        }

        val startX = barX + 60 * percentStart
        val endX = barX + 60 * percentEnd

        if (mouseButton == 0) {
            val nearStart = abs(mouseX - startX) <= 6
            val nearEnd = abs(mouseX - endX) <= 6

            if (nearStart || nearEnd || RenderUtil.isHovering(barX, barY, 60f, 6f, mouseX, mouseY)) {
                if (nearStart && nearEnd) {
                    if (abs(mouseX - startX) <= abs(mouseX - endX)) draggingLeft = true else draggingRight = true
                } else if (nearStart) {
                    draggingLeft = true
                } else if (nearEnd) {
                    draggingRight = true
                } else {
                    if (abs(mouseX - startX) < abs(mouseX - endX)) draggingLeft = true else draggingRight = true
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            draggingLeft = false
            draggingRight = false
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}