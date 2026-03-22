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
 * Frame time tracking utility for performance analysis
 *
 * Tracks frame times and calculates percentile statistics (1% low, 0.1% low)
 * which are important metrics for identifying performance issues and stutters.
 *
 * Thread-safe implementation with minimal overhead using ring buffer storage.
 *
 * @author FDPClient
 */
object FrameTimeTracker {

    private val lock = ReentrantReadWriteLock()
    private const val BUFFER_SIZE = 1000

    // Ring buffer for frame times (in nanoseconds)
    private val frameTimes = LongArray(BUFFER_SIZE)
    private var writeIndex = 0
    private var sampleCount = 0

    /**
     * Record a frame time
     *
     * @param frameTimeNanos Frame rendering time in nanoseconds
     */
    fun recordFrame(frameTimeNanos: Long) {
        lock.write {
            frameTimes[writeIndex] = frameTimeNanos
            writeIndex = (writeIndex + 1) % BUFFER_SIZE

            if (sampleCount < BUFFER_SIZE) {
                sampleCount++
            }
        }
    }

    /**
     * Get 1% low frame time (99th percentile)
     *
     * This represents the worst 1% of frame times, useful for identifying
     * performance drops and stuttering issues.
     *
     * @return 99th percentile frame time in milliseconds
     */
    fun get1PercentLow(): Double {
        return lock.read {
            if (sampleCount == 0) {
                return 0.0
            }

            val percentile = 0.99
            calculatePercentile(percentile)
        }
    }

    /**
     * Get 0.1% low frame time (99.9th percentile)
     *
     * This represents the worst 0.1% of frame times, useful for identifying
     * severe performance drops and extreme stuttering.
     *
     * @return 99.9th percentile frame time in milliseconds
     */
    fun get01PercentLow(): Double {
        return lock.read {
            if (sampleCount == 0) {
                return 0.0
            }

            val percentile = 0.999
            calculatePercentile(percentile)
        }
    }

    /**
     * Calculate percentile frame time
     * Must be called while holding read lock
     *
     * @param percentile Percentile to calculate (0.0 to 1.0)
     * @return Frame time at the given percentile in milliseconds
     */
    private fun calculatePercentile(percentile: Double): Double {
        // Copy active samples to avoid sorting the ring buffer
        val samples = LongArray(sampleCount)

        if (sampleCount < BUFFER_SIZE) {
            // Buffer not yet full, copy from start
            System.arraycopy(frameTimes, 0, samples, 0, sampleCount)
        } else {
            // Buffer full, copy in correct order
            val firstPart = BUFFER_SIZE - writeIndex
            System.arraycopy(frameTimes, writeIndex, samples, 0, firstPart)
            System.arraycopy(frameTimes, 0, samples, firstPart, writeIndex)
        }

        // Sort samples to calculate percentile
        samples.sort()

        // Calculate percentile index
        val index = ((sampleCount - 1) * percentile).toInt()
        val frameTimeNanos = samples[index]

        // Convert nanoseconds to milliseconds
        return frameTimeNanos / 1_000_000.0
    }

    /**
     * Get average frame time
     *
     * @return Average frame time in milliseconds
     */
    fun getAverage(): Double {
        return lock.read {
            if (sampleCount == 0) {
                return 0.0
            }

            var sum = 0L
            for (i in 0 until sampleCount) {
                sum += frameTimes[i]
            }

            (sum / sampleCount) / 1_000_000.0
        }
    }

    /**
     * Get minimum frame time
     *
     * @return Minimum frame time in milliseconds
     */
    fun getMin(): Double {
        return lock.read {
            if (sampleCount == 0) {
                return 0.0
            }

            var min = Long.MAX_VALUE
            for (i in 0 until sampleCount) {
                if (frameTimes[i] < min) {
                    min = frameTimes[i]
                }
            }

            min / 1_000_000.0
        }
    }

    /**
     * Get maximum frame time
     *
     * @return Maximum frame time in milliseconds
     */
    fun getMax(): Double {
        return lock.read {
            if (sampleCount == 0) {
                return 0.0
            }

            var max = 0L
            for (i in 0 until sampleCount) {
                if (frameTimes[i] > max) {
                    max = frameTimes[i]
                }
            }

            max / 1_000_000.0
        }
    }

    /**
     * Reset all frame time statistics
     */
    fun reset() {
        lock.write {
            writeIndex = 0
            sampleCount = 0
            // No need to clear the array, we track valid samples with sampleCount
        }
    }

    /**
     * Get number of recorded samples
     *
     * @return Number of frame time samples currently stored
     */
    fun getSampleCount(): Int {
        return lock.read { sampleCount }
    }
}
