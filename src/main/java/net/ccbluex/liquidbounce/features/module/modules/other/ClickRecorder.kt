/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.TickEndEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import org.lwjgl.opengl.Display
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ClickRecorder : Module("ClickRecorder", Category.OTHER) {

    private val recordRightClick by boolean("RecordRightClick", false)
    private val recordMiddleClick by boolean("RecordMiddleClick", false)

    private val ticks = mutableListOf<Int>()
    private val leftClicks = mutableListOf<Int>()
    private val rightClicks = mutableListOf<Int>()
    private val middleClicks = mutableListOf<Int>()

    private var chart: XYChart? = null
    private var failed = false

    override fun onEnable() {
        updateRecordInfo(true)

        try {
            chart = XYChart(Display.getWidth() * 2, Display.getHeight() / 2).apply {
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
        } catch (e: Exception) {
            e.printStackTrace()
            chat("Failed to start recording clicks, disabling module")
            nextTick {
                failed = true
                state = false
            }

            return
        }

        chat("Started recording clicks.")
    }

    val onTickEnding = handler<TickEndEvent> {
        updateRecordInfo()

        chart?.updateXYSeries("Left Clicks", ticks, leftClicks, null)
        chart?.updateXYSeries("Right Clicks", ticks, rightClicks, null)
        chart?.updateXYSeries("Middle Clicks", ticks, middleClicks, null)

    }

    override fun onDisable() {
        if (!failed) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val formattedDateTime = currentDateTime.format(formatter)

            saveChart("clicks_$formattedDateTime.png", FileManager.dir)
        }

        failed = false
        ticks.clear()
        leftClicks.clear()
        rightClicks.clear()
        middleClicks.clear()
    }

    private fun saveChart(fileName: String, mcDir: File) {
        val file = File(mcDir, fileName)

        try {
            BitmapEncoder.saveBitmap(chart, file.absolutePath, BitmapEncoder.BitmapFormat.PNG)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            chat("Saved as $fileName in $mcDir")
        }
    }

    private fun updateRecordInfo(wasPreviousTick: Boolean = false) {
        val runTimeTicks = runTimeTicks - if (wasPreviousTick) 1 else 0

        ticks += runTimeTicks
        leftClicks += CPSCounter.getCPS(CPSCounter.MouseButton.LEFT, runTimeTicks)

        rightClicks += if (recordRightClick) {
            CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT, runTimeTicks)
        } else 0

        middleClicks += if (recordMiddleClick) {
            CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE, runTimeTicks)
        } else 0
    }
}