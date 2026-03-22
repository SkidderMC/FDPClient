/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.performance.PerformanceMonitor
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color
import java.text.DecimalFormat

/**
 * Performance statistics HUD element
 *
 * Displays real-time performance metrics including FPS, frame times,
 * memory usage, GC stats, and more from the PerformanceMonitor.
 */
@ElementInfo(name = "PerformanceStats")
class PerformanceStats(
    x: Double = 10.0,
    y: Double = 80.0,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element("PerformanceStats", x, y, scale, side) {

    companion object {
        private val DECIMAL_FORMAT = DecimalFormat("0.00")
        private val DECIMAL_FORMAT_1 = DecimalFormat("0.0")
    }

    // Display options
    private val showFPS by boolean("ShowFPS", true)
    private val showFrameTime by boolean("ShowFrameTime", true)
    private val showFramePercentiles by boolean("ShowFramePercentiles", false)
    private val showMemory by boolean("ShowMemory", true)
    private val showGC by boolean("ShowGC", false)
    private val showThreads by boolean("ShowThreads", false)
    private val showCPU by boolean("ShowCPU", false)
    private val showAllocation by boolean("ShowAllocation", false)

    // Style options
    private val fontValue by font("Font", Fonts.minecraftFont)
    private val shadow by boolean("Shadow", true)
    private val background by boolean("Background", true)
    private val backgroundAlpha by int("BackgroundAlpha", 100, 0..255) { background }

    private val colorMode by choices("ColorMode", arrayOf("Single", "FPS-Based"), "Single")
    private val color = ColorSettingsInteger(this, "Color", applyMax = true) { colorMode == "Single" }

    // FPS thresholds for color coding
    private val goodFPSThreshold by int("GoodFPSThreshold", 60, 30..144) { colorMode == "FPS-Based" }
    private val mediumFPSThreshold by int("MediumFPSThreshold", 30, 15..60) { colorMode == "FPS-Based" }

    override fun drawElement(): Border {
        val fontRenderer = fontValue as GameFontRenderer
        val lines = mutableListOf<String>()
        val monitor = PerformanceMonitor

        // FPS information
        if (showFPS) {
            val fps = monitor.currentFPS
            val avgFps1s = monitor.averageFPS1s
            val minFps = monitor.minFPS
            val maxFps = monitor.maxFPS

            lines.add("FPS: $fps (avg: ${DECIMAL_FORMAT_1.format(avgFps1s)}, min: $minFps, max: $maxFps)")
        }

        // Frame time information
        if (showFrameTime) {
            val frameTime = monitor.lastFrameTimeMs
            val avgFrameTime = monitor.averageFrameTime1s
            lines.add("Frame: ${DECIMAL_FORMAT.format(frameTime)}ms (avg: ${DECIMAL_FORMAT.format(avgFrameTime)}ms)")
        }

        // Frame percentiles (1% low, 0.1% low)
        if (showFramePercentiles) {
            val low1 = monitor.frameTime1PercentLow
            val low01 = monitor.frameTime01PercentLow
            lines.add("1%: ${DECIMAL_FORMAT.format(low1)}ms | 0.1%: ${DECIMAL_FORMAT.format(low01)}ms")
        }

        // Memory information
        if (showMemory) {
            val used = monitor.usedMemoryMB
            val total = monitor.totalMemoryMB
            val percent = monitor.memoryUsagePercent
            lines.add("Memory: ${used}MB / ${total}MB ($percent%)")
        }

        // GC information
        if (showGC) {
            val gcCount = monitor.gcCount
            val gcTime = monitor.gcTime
            val avgGC = if (gcCount > 0) gcTime / gcCount else 0
            lines.add("GC: $gcCount collections, ${gcTime}ms total (avg: ${avgGC}ms)")
        }

        // Thread count
        if (showThreads) {
            val threads = monitor.threadCount
            lines.add("Threads: $threads")
        }

        // CPU usage
        if (showCPU) {
            val processCpu = (monitor.processCpuLoad * 100).toInt()
            val systemCpu = (monitor.systemCpuLoad * 100).toInt()
            if (processCpu > 0 || systemCpu > 0) {
                lines.add("CPU: Process ${processCpu}% | System ${systemCpu}%")
            }
        }

        // Allocation rate
        if (showAllocation) {
            val allocRate = monitor.allocationRate
            if (allocRate > 0) {
                lines.add("Alloc: ${DECIMAL_FORMAT.format(allocRate)} MB/s")
            }
        }

        // Calculate dimensions
        var maxWidth = 0F
        var totalHeight = 0F

        for (line in lines) {
            val width = fontRenderer.getStringWidth(line)
            if (width > maxWidth) {
                maxWidth = width.toFloat()
            }
            totalHeight += fontRenderer.height.toFloat()
        }

        // Add padding
        val padding = 4F
        maxWidth += padding * 2
        totalHeight += padding * 2

        // Draw background
        if (background) {
            val bgColor = Color(0, 0, 0, backgroundAlpha).rgb
            RenderUtils.drawRect(
                -padding,
                -padding,
                maxWidth - padding,
                totalHeight - padding,
                bgColor
            )
        }

        // Determine text color
        val textColor = when (colorMode) {
            "FPS-Based" -> {
                val fps = monitor.currentFPS
                when {
                    fps >= goodFPSThreshold -> Color(0, 255, 0).rgb // Green
                    fps >= mediumFPSThreshold -> Color(255, 255, 0).rgb // Yellow
                    else -> Color(255, 0, 0).rgb // Red
                }
            }
            else -> color.color().rgb
        }

        // Draw text lines
        var yOffset = 0F
        for (line in lines) {
            fontRenderer.drawString(
                line,
                0F,
                yOffset,
                textColor,
                shadow
            )
            yOffset += fontRenderer.height.toFloat()
        }

        return Border(
            -padding,
            -padding,
            maxWidth - padding,
            totalHeight - padding
        )
    }
}
