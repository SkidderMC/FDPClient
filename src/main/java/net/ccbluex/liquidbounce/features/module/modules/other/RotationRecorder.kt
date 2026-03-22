/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.lastRotations
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYSeries
import org.lwjgl.opengl.Display
import kotlin.math.absoluteValue

object RotationRecorder : ChartRecorderModule("RotationRecorder") {

    private val captureNegativeNumbers by boolean("CaptureNegativeNumbers", false)
    private const val MAX_RECORDED_ROTATIONS = 1000

    private val ticks = mutableListOf<Double>()
    private val yawDiffs = mutableListOf<Double>()
    private val pitchDiffs = mutableListOf<Double>()

    override fun onEnable() {
        updateRecordInfo(true)

        initializeChart(
            failureMessage = "Failed to start recording rotations, disabling module",
            startMessage = "Started recording rotations.",
        ) {
            XYChart(Display.getWidth(), Display.getHeight()).apply {
                title = "Yaw and Pitch Differences Over Time"
                xAxisTitle = "Time (ticks)"
                yAxisTitle = "Differences (degrees)"

                addSeries("Yaw Differences", ticks.toDoubleArray(), yawDiffs.toDoubleArray()).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.BLUE
                    isSmooth = true
                }

                addSeries("Pitch Differences", ticks.toDoubleArray(), pitchDiffs.toDoubleArray()).apply {
                    xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                    lineColor = java.awt.Color.RED
                    isSmooth = true
                }
            }
        }
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST || failed) {
            return@handler
        }

        updateRecordInfo()

        chart?.updateXYSeries("Yaw Differences", ticks.toDoubleArray(), yawDiffs.toDoubleArray(), null)
        chart?.updateXYSeries("Pitch Differences", ticks.toDoubleArray(), pitchDiffs.toDoubleArray(), null)
    }

    override fun onDisable() {
        if (!failed) {
            saveChart("rotations")
        }

        failed = false
        ticks.clear()
        yawDiffs.clear()
        pitchDiffs.clear()
    }

    private fun updateRecordInfo(wasPreviousTick: Boolean = false) {
        var yawDiff = angleDifference(serverRotation.yaw, lastRotations[1].yaw)
        var pitchDiff = angleDifference(serverRotation.pitch, lastRotations[1].pitch)

        if (!captureNegativeNumbers) {
            yawDiff = yawDiff.absoluteValue
            pitchDiff = pitchDiff.absoluteValue
        }

        if (limitReached(ticks.size, MAX_RECORDED_ROTATIONS, "rotation")) {
            return
        }

        ticks += runTimeTicks.toDouble() - if (wasPreviousTick) 1 else 0
        yawDiffs += yawDiff.toDouble()
        pitchDiffs += pitchDiff.toDouble()
    }
}
