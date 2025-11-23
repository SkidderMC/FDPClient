/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.lastRotations
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
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
import kotlin.math.absoluteValue

object RotationRecorder : Module("RotationRecorder", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val captureNegativeNumbers by boolean("CaptureNegativeNumbers", false)

    private val ticks = mutableListOf<Double>()
    private val yawDiffs = mutableListOf<Double>()
    private val pitchDiffs = mutableListOf<Double>()

    private var chart: XYChart? = null
    private var failed = false

    override fun onEnable() {
        updateRecordInfo(true)

        try {
            chart = XYChart(Display.getWidth(), Display.getHeight()).apply {
                title = "Yaw and Pitch Differences Over Time"
                xAxisTitle = "Time (ticks)"
                yAxisTitle = "Differences (degrees)"

                // Add series to the chart
                addSeries("Yaw Differences", ticks.toDoubleArray(), yawDiffs.toDoubleArray()).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.BLUE // Set yaw line color to blue
                    isSmooth = true
                }

                addSeries("Pitch Differences", ticks.toDoubleArray(), pitchDiffs.toDoubleArray()).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.RED // Set pitch line color to red
                    isSmooth = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            chat("Failed to start recording rotations, disabling module")
            nextTick {
                failed = true
                state = false
            }

            return
        }

        chat("Started recording rotations.")
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST || failed)
            return@handler

        updateRecordInfo()

        // Update the series with new data
        chart?.updateXYSeries("Yaw Differences", ticks.toDoubleArray(), yawDiffs.toDoubleArray(), null)
        chart?.updateXYSeries("Pitch Differences", ticks.toDoubleArray(), pitchDiffs.toDoubleArray(), null)
    }

    override fun onDisable() {
        if (!failed) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val formattedDateTime = currentDateTime.format(formatter)

            saveChart("rotations_$formattedDateTime.png", FileManager.dir)
        }

        failed = false
        ticks.clear()
        yawDiffs.clear()
        pitchDiffs.clear()
    }

    private fun saveChart(fileName: String, mcDir: File) {
        val file = File(mcDir, fileName)

        // Save the chart as an image
        try {
            BitmapEncoder.saveBitmap(chart, file.absolutePath, BitmapEncoder.BitmapFormat.PNG)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            chat("Saved as $fileName in $mcDir")
        }
    }

    private fun updateRecordInfo(wasPreviousTick: Boolean = false) {
        var yawDiff = angleDifference(serverRotation.yaw, lastRotations[1].yaw)
        var pitchDiff = angleDifference(serverRotation.pitch, lastRotations[1].pitch)

        if (!captureNegativeNumbers) {
            yawDiff = yawDiff.absoluteValue
            pitchDiff = pitchDiff.absoluteValue
        }

        ticks.add(runTimeTicks.toDouble() - if (wasPreviousTick) 1 else 0)
        yawDiffs.add(yawDiff.toDouble())
        pitchDiffs.add(pitchDiff.toDouble())
    }

}