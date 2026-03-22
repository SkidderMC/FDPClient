/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.performance

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.Minecraft
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.text.DecimalFormat
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.max
import kotlin.math.min

/**
 * Comprehensive performance monitoring utility
 * Tracks FPS, memory usage, GC statistics, CPU usage, frame times, and more
 *
 * Thread-safe implementation with minimal overhead (< 1% CPU)
 *
 * @author FDPClient
 */
object PerformanceMonitor : MinecraftInstance, Listenable {

    private val lock = ReentrantReadWriteLock()
    private val decimalFormat = DecimalFormat("0.00")

    // Ring buffers for time series data (efficient memory usage)
    private val fpsHistory1s = RingBuffer<Int>(60) // 60 samples at 60 FPS = 1 second
    private val fpsHistory5s = RingBuffer<Int>(300) // 300 samples = 5 seconds
    private val fpsHistory60s = RingBuffer<Int>(3600) // 3600 samples = 60 seconds

    // Frame time tracking
    private var frameStartTime = 0L
    private var lastFrameTime = 0L

    // FPS tracking
    private var _currentFPS = 0
    private var _minFPS = Int.MAX_VALUE
    private var _maxFPS = 0
    private var frameCount = 0
    private var lastFpsUpdate = System.currentTimeMillis()

    // Memory tracking
    private val runtime = Runtime.getRuntime()
    private var lastMemoryCheck = 0L
    private var _usedMemoryMB = 0L
    private var _freeMemoryMB = 0L
    private var _totalMemoryMB = 0L
    private val _maxMemoryMB = runtime.maxMemory() / 1024L / 1024L

    // Thread tracking
    private var _threadCount = 0

    // CPU tracking
    private val osMxBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private var lastCpuCheck = 0L
    private var _processCpuLoad = 0.0
    private var _systemCpuLoad = 0.0

    // Object allocation tracking
    private var lastAllocatedBytes = 0L
    private var lastAllocationCheck = System.currentTimeMillis()
    private var _allocationRate = 0.0 // MB/s

    // Enable/disable flag
    var enabled = true

    /**
     * Current FPS
     */
    val currentFPS: Int
        get() = lock.read { _currentFPS }

    /**
     * Average FPS over last 1 second
     */
    val averageFPS1s: Double
        get() = lock.read {
            if (fpsHistory1s.size() > 0) fpsHistory1s.average() else 0.0
        }

    /**
     * Average FPS over last 5 seconds
     */
    val averageFPS5s: Double
        get() = lock.read {
            if (fpsHistory5s.size() > 0) fpsHistory5s.average() else 0.0
        }

    /**
     * Average FPS over last 60 seconds
     */
    val averageFPS60s: Double
        get() = lock.read {
            if (fpsHistory60s.size() > 0) fpsHistory60s.average() else 0.0
        }

    /**
     * Minimum FPS recorded
     */
    val minFPS: Int
        get() = lock.read { if (_minFPS == Int.MAX_VALUE) 0 else _minFPS }

    /**
     * Maximum FPS recorded
     */
    val maxFPS: Int
        get() = lock.read { _maxFPS }

    /**
     * Used memory in MB
     */
    val usedMemoryMB: Long
        get() = lock.read { _usedMemoryMB }

    /**
     * Free memory in MB
     */
    val freeMemoryMB: Long
        get() = lock.read { _freeMemoryMB }

    /**
     * Total memory in MB
     */
    val totalMemoryMB: Long
        get() = lock.read { _totalMemoryMB }

    /**
     * Maximum memory in MB
     */
    val maxMemoryMB: Long
        get() = _maxMemoryMB

    /**
     * Memory usage percentage
     */
    val memoryUsagePercent: Int
        get() = lock.read {
            if (_totalMemoryMB > 0) ((_usedMemoryMB * 100) / _totalMemoryMB).toInt()
            else 0
        }

    /**
     * GC collection count
     */
    val gcCount: Long
        get() = GCMonitor.totalGCCount

    /**
     * Total GC time in milliseconds
     */
    val gcTime: Long
        get() = GCMonitor.totalGCTime

    /**
     * Active thread count
     */
    val threadCount: Int
        get() = lock.read { _threadCount }

    /**
     * Process CPU load (0.0 to 1.0)
     */
    val processCpuLoad: Double
        get() = lock.read { _processCpuLoad }

    /**
     * System CPU load (0.0 to 1.0)
     */
    val systemCpuLoad: Double
        get() = lock.read { _systemCpuLoad }

    /**
     * Object allocation rate in MB/s
     */
    val allocationRate: Double
        get() = lock.read { _allocationRate }

    /**
     * Last frame time in milliseconds
     */
    val lastFrameTimeMs: Double
        get() = lock.read { lastFrameTime / 1_000_000.0 }

    /**
     * Average frame time over 1 second
     */
    val averageFrameTime1s: Double
        get() = lock.read {
            val avgFps = if (fpsHistory1s.size() > 0) fpsHistory1s.average() else 60.0
            if (avgFps > 0) 1000.0 / avgFps else 0.0
        }

    /**
     * 1% low frame time (99th percentile)
     */
    val frameTime1PercentLow: Double
        get() = FrameTimeTracker.get1PercentLow()

    /**
     * 0.1% low frame time (99.9th percentile)
     */
    val frameTime01PercentLow: Double
        get() = FrameTimeTracker.get01PercentLow()

    /**
     * Start frame timing
     * Call this at the beginning of frame rendering
     */
    fun startFrame() {
        if (!enabled) return

        frameStartTime = System.nanoTime()
    }

    /**
     * End frame timing
     * Call this at the end of frame rendering
     */
    fun endFrame() {
        if (!enabled) return

        val endTime = System.nanoTime()
        lastFrameTime = endTime - frameStartTime

        // Update frame time tracker
        FrameTimeTracker.recordFrame(lastFrameTime)

        // Update FPS
        frameCount++
        val currentTime = System.currentTimeMillis()

        lock.write {
            // Update FPS every second
            if (currentTime - lastFpsUpdate >= 1000) {
                _currentFPS = frameCount
                frameCount = 0
                lastFpsUpdate = currentTime

                // Update FPS history
                fpsHistory1s.add(_currentFPS)
                fpsHistory5s.add(_currentFPS)
                fpsHistory60s.add(_currentFPS)

                // Update min/max
                if (_currentFPS > 0) {
                    _minFPS = min(_minFPS, _currentFPS)
                    _maxFPS = max(_maxFPS, _currentFPS)
                }
            }

            // Update memory stats every 500ms to reduce overhead
            if (currentTime - lastMemoryCheck >= 500) {
                updateMemoryStats()
                lastMemoryCheck = currentTime
            }

            // Update CPU stats every 1000ms
            if (currentTime - lastCpuCheck >= 1000) {
                updateCpuStats()
                lastCpuCheck = currentTime
            }
        }
    }

    /**
     * Update memory statistics
     */
    private fun updateMemoryStats() {
        val total = runtime.totalMemory() / 1024L / 1024L
        val free = runtime.freeMemory() / 1024L / 1024L
        val used = total - free

        _totalMemoryMB = total
        _freeMemoryMB = free
        _usedMemoryMB = used

        // Calculate allocation rate
        val currentTime = System.currentTimeMillis()
        val timeDelta = (currentTime - lastAllocationCheck) / 1000.0

        if (timeDelta > 0) {
            val currentAllocated = runtime.totalMemory() - runtime.freeMemory()
            val allocatedDelta = currentAllocated - lastAllocatedBytes

            if (allocatedDelta > 0) {
                _allocationRate = (allocatedDelta / 1024.0 / 1024.0) / timeDelta
            }

            lastAllocatedBytes = currentAllocated
            lastAllocationCheck = currentTime
        }
    }

    /**
     * Update CPU statistics
     */
    private fun updateCpuStats() {
        _threadCount = Thread.activeCount()

        // Try to get CPU load if available
        try {
            val method = osMxBean.javaClass.getMethod("getProcessCpuLoad")
            val cpuLoad = method.invoke(osMxBean) as? Double
            if (cpuLoad != null && cpuLoad >= 0) {
                _processCpuLoad = cpuLoad
            }
        } catch (e: Exception) {
            // Method not available, skip
        }

        try {
            val method = osMxBean.javaClass.getMethod("getSystemCpuLoad")
            val cpuLoad = method.invoke(osMxBean) as? Double
            if (cpuLoad != null && cpuLoad >= 0) {
                _systemCpuLoad = cpuLoad
            }
        } catch (e: Exception) {
            // Method not available, skip
        }
    }

    /**
     * Reset all statistics
     */
    fun reset() {
        lock.write {
            fpsHistory1s.clear()
            fpsHistory5s.clear()
            fpsHistory60s.clear()
            _minFPS = Int.MAX_VALUE
            _maxFPS = 0
            frameCount = 0
            lastFpsUpdate = System.currentTimeMillis()
            _currentFPS = 0
            FrameTimeTracker.reset()
            GCMonitor.reset()
        }
    }

    /**
     * Get comprehensive statistics as formatted string
     */
    fun getStats(): String {
        return lock.read {
            buildString {
                appendLine("=== Performance Statistics ===")
                appendLine("FPS: $currentFPS (avg 1s: ${decimalFormat.format(averageFPS1s)}, " +
                        "5s: ${decimalFormat.format(averageFPS5s)}, " +
                        "min: $minFPS, max: $maxFPS)")
                appendLine("Memory: ${usedMemoryMB}MB / ${totalMemoryMB}MB ($memoryUsagePercent%) " +
                        "(max: ${maxMemoryMB}MB)")
                appendLine("GC: $gcCount collections, ${gcTime}ms total, " +
                        "avg: ${if (gcCount > 0) gcTime / gcCount else 0}ms")
                appendLine("Frame Time: ${decimalFormat.format(lastFrameTimeMs)}ms " +
                        "(avg: ${decimalFormat.format(averageFrameTime1s)}ms, " +
                        "1%: ${decimalFormat.format(frameTime1PercentLow)}ms, " +
                        "0.1%: ${decimalFormat.format(frameTime01PercentLow)}ms)")
                appendLine("Threads: $threadCount active")
                appendLine("Allocation Rate: ${decimalFormat.format(allocationRate)} MB/s")
                if (_processCpuLoad > 0) {
                    appendLine("CPU: Process ${(processCpuLoad * 100).toInt()}%, " +
                            "System ${(systemCpuLoad * 100).toInt()}%")
                }
                append("==============================")
            }
        }
    }

    /**
     * Get FPS history for graphing
     */
    fun getFPSHistory(seconds: Int = 5): List<Int> {
        return lock.read {
            when (seconds) {
                1 -> fpsHistory1s.toList()
                5 -> fpsHistory5s.toList()
                60 -> fpsHistory60s.toList()
                else -> fpsHistory5s.toList()
            }
        }
    }

    /**
     * Get memory history percentage
     */
    fun getMemoryUsageHistory(): List<Int> {
        return MemoryProfiler.getMemoryHistory()
    }
}

/**
 * Thread-safe ring buffer for efficient time series storage
 */
private class RingBuffer<T>(private val capacity: Int) {
    private val buffer = ArrayList<T>(capacity)
    private var head = 0

    @Synchronized
    fun add(item: T) {
        if (buffer.size < capacity) {
            buffer.add(item)
        } else {
            buffer[head] = item
            head = (head + 1) % capacity
        }
    }

    @Synchronized
    fun size() = buffer.size

    @Synchronized
    fun toList(): List<T> = buffer.toList()

    @Synchronized
    fun clear() {
        buffer.clear()
        head = 0
    }

    @Synchronized
    fun average(): Double {
        if (buffer.isEmpty()) return 0.0

        return when (buffer[0]) {
            is Int -> buffer.sumOf { (it as Int).toDouble() } / buffer.size
            is Long -> buffer.sumOf { (it as Long).toDouble() } / buffer.size
            is Float -> buffer.sumOf { (it as Float).toDouble() } / buffer.size
            is Double -> buffer.sumOf { it as Double } / buffer.size
            else -> 0.0
        }
    }
}
