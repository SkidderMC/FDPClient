/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.performance

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Memory profiling utility that tracks memory usage over time
 *
 * Stores memory usage samples (as percentage) for visualization and analysis.
 * Automatically updates samples or can be updated on demand.
 *
 * Thread-safe implementation with minimal overhead.
 *
 * @author FDPClient
 */
object MemoryProfiler {

    private val lock = ReentrantReadWriteLock()
    private val runtime = Runtime.getRuntime()

    // Store 60 seconds of history at 1 sample per second
    private const val HISTORY_SIZE = 60
    private val memoryHistory = IntArray(HISTORY_SIZE)
    private var writeIndex = 0
    private var sampleCount = 0
    private var lastUpdateTime = System.currentTimeMillis()
    private var autoUpdate = true

    /**
     * Get memory usage history as percentage values
     *
     * Returns the last 60 seconds of memory usage samples.
     * Each value represents the percentage of total allocated memory in use.
     *
     * @return List of memory usage percentages (0-100)
     */
    fun getMemoryHistory(): List<Int> {
        lock.read {
            // Update if auto-update is enabled and enough time has passed
            if (autoUpdate) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= 1000) {
                    lock.write {
                        recordSample()
                    }
                }
            }

            // Return history in chronological order
            val result = mutableListOf<Int>()

            if (sampleCount < HISTORY_SIZE) {
                // Buffer not yet full
                for (i in 0 until sampleCount) {
                    result.add(memoryHistory[i])
                }
            } else {
                // Buffer full, return in correct order
                for (i in 0 until HISTORY_SIZE) {
                    val index = (writeIndex + i) % HISTORY_SIZE
                    result.add(memoryHistory[index])
                }
            }

            return result
        }
    }

    /**
     * Record a memory usage sample
     * Should be called while holding write lock
     */
    private fun recordSample() {
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val used = total - free

        // Calculate percentage
        val percentage = if (total > 0) {
            ((used * 100) / total).toInt()
        } else {
            0
        }

        memoryHistory[writeIndex] = percentage
        writeIndex = (writeIndex + 1) % HISTORY_SIZE

        if (sampleCount < HISTORY_SIZE) {
            sampleCount++
        }

        lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * Manually update memory statistics
     *
     * Call this to force an update of memory usage samples.
     * Useful when auto-update is disabled.
     */
    fun update() {
        lock.write {
            recordSample()
        }
    }

    /**
     * Get current memory usage percentage
     *
     * @return Current memory usage as percentage (0-100)
     */
    fun getCurrentUsagePercent(): Int {
        return lock.read {
            val total = runtime.totalMemory()
            val free = runtime.freeMemory()
            val used = total - free

            if (total > 0) {
                ((used * 100) / total).toInt()
            } else {
                0
            }
        }
    }

    /**
     * Get average memory usage over the history period
     *
     * @return Average memory usage percentage (0-100)
     */
    fun getAverageUsagePercent(): Int {
        return lock.read {
            if (sampleCount == 0) {
                return 0
            }

            var sum = 0
            for (i in 0 until sampleCount) {
                sum += memoryHistory[i]
            }

            sum / sampleCount
        }
    }

    /**
     * Get peak memory usage over the history period
     *
     * @return Peak memory usage percentage (0-100)
     */
    fun getPeakUsagePercent(): Int {
        return lock.read {
            if (sampleCount == 0) {
                return 0
            }

            var peak = 0
            for (i in 0 until sampleCount) {
                if (memoryHistory[i] > peak) {
                    peak = memoryHistory[i]
                }
            }

            peak
        }
    }

    /**
     * Get minimum memory usage over the history period
     *
     * @return Minimum memory usage percentage (0-100)
     */
    fun getMinUsagePercent(): Int {
        return lock.read {
            if (sampleCount == 0) {
                return 0
            }

            var min = 100
            for (i in 0 until sampleCount) {
                if (memoryHistory[i] < min) {
                    min = memoryHistory[i]
                }
            }

            min
        }
    }

    /**
     * Reset all memory statistics
     */
    fun reset() {
        lock.write {
            writeIndex = 0
            sampleCount = 0
            lastUpdateTime = System.currentTimeMillis()
            // No need to clear array, we track valid samples with sampleCount
        }
    }

    /**
     * Enable or disable automatic updates
     *
     * When enabled, memory samples are automatically recorded when
     * getMemoryHistory() is called (if enough time has passed).
     *
     * @param enabled Whether to enable automatic updates
     */
    fun setAutoUpdate(enabled: Boolean) {
        lock.write {
            autoUpdate = enabled
        }
    }

    /**
     * Check if auto-update is enabled
     *
     * @return True if auto-update is enabled
     */
    fun isAutoUpdateEnabled(): Boolean {
        return lock.read { autoUpdate }
    }

    /**
     * Get number of recorded samples
     *
     * @return Number of memory samples currently stored
     */
    fun getSampleCount(): Int {
        return lock.read { sampleCount }
    }
}
