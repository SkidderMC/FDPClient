/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.TickEndEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import org.lwjgl.opengl.Display

object ClickRecorder : ChartRecorderModule("ClickRecorder") {

    private val recordRightClick by boolean("RecordRightClick", false)
    private val recordMiddleClick by boolean("RecordMiddleClick", false)
    private const val MAX_RECORDED_CLICKS = 1000

    private val ticks = mutableListOf<Int>()
    private val leftClicks = mutableListOf<Int>()
    private val rightClicks = mutableListOf<Int>()
    private val middleClicks = mutableListOf<Int>()

    override fun onEnable() {
        updateRecordInfo(true)

        initializeChart(
            failureMessage = "Failed to start recording clicks, disabling module",
            startMessage = "Started recording clicks.",
        ) {
            XYChart(Display.getWidth() * 2, Display.getHeight() / 2).apply {
                title = "Clicks Over Time"
                xAxisTitle = "Time (ticks)"
                yAxisTitle = "Clicks"

                addSeries("Left Clicks", ticks, leftClicks).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.RED
                    isSmooth = true
                }
                addSeries("Right Clicks", ticks, rightClicks).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.BLUE
                    isSmooth = true
                }
                addSeries("Middle Clicks", ticks, middleClicks).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.GREEN
                    isSmooth = true
                }
            }
        }
    }

    val onTickEnding = handler<TickEndEvent> {
        updateRecordInfo()

        chart?.updateXYSeries("Left Clicks", ticks, leftClicks, null)
        chart?.updateXYSeries("Right Clicks", ticks, rightClicks, null)
        chart?.updateXYSeries("Middle Clicks", ticks, middleClicks, null)
    }

    override fun onDisable() {
        if (!failed) {
            saveChart("clicks")
        }

        failed = false
        ticks.clear()
        leftClicks.clear()
        rightClicks.clear()
        middleClicks.clear()
    }

    private fun updateRecordInfo(wasPreviousTick: Boolean = false) {
        val runtimeTick = runTimeTicks - if (wasPreviousTick) 1 else 0

        if (limitReached(ticks.size, MAX_RECORDED_CLICKS, "click")) {
            return
        }

        ticks += runtimeTick
        leftClicks += CPSCounter.getCPS(CPSCounter.MouseButton.LEFT, runtimeTick)
        rightClicks += if (recordRightClick) CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT, runtimeTick) else 0
        middleClicks += if (recordMiddleClick) CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE, runtimeTick) else 0
    }
}
