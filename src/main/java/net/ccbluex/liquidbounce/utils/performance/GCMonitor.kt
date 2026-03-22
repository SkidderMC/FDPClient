/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.performance

import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import javax.management.Notification
import javax.management.NotificationEmitter
import javax.management.NotificationListener
import javax.management.openmbean.CompositeData
import java.util.concurrent.atomic.AtomicLong

/**
 * GC monitoring utility that tracks garbage collection events
 *
 * Listens to all GC events and provides statistics about total collections
 * and time spent in GC. Thread-safe implementation using atomic operations.
 *
 * @author FDPClient
 */
object GCMonitor : NotificationListener {

    private val _totalGCCount = AtomicLong(0)
    private val _totalGCTime = AtomicLong(0)

    /**
     * Total number of garbage collections that have occurred
     */
    val totalGCCount: Long
        get() = _totalGCCount.get()

    /**
     * Total time spent in garbage collection (milliseconds)
     */
    val totalGCTime: Long
        get() = _totalGCTime.get()

    init {
        // Register notification listener for all GC beans
        try {
            val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
            for (gcBean in gcBeans) {
                if (gcBean is NotificationEmitter) {
                    gcBean.addNotificationListener(this, null, null)
                }
            }
        } catch (e: Exception) {
            // GC monitoring not available, continue without it
            System.err.println("GCMonitor: Failed to register GC notification listener: ${e.message}")
        }
    }

    /**
     * Handle GC notification events
     */
    override fun handleNotification(notification: Notification, handback: Any?) {
        try {
            // Check if this is a GC notification
            if (notification.type == "com.sun.management.gc.notification") {
                val userData = notification.userData

                if (userData is CompositeData) {
                    val gcInfo = userData.get("gcInfo") as? CompositeData

                    if (gcInfo != null) {
                        // Extract GC duration
                        val duration = gcInfo.get("duration") as? Long

                        if (duration != null) {
                            _totalGCCount.incrementAndGet()
                            _totalGCTime.addAndGet(duration)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Silently ignore notification processing errors to avoid spam
            // These can occur during JVM shutdown or with unusual GC configurations
        }
    }

    /**
     * Reset all GC statistics to zero
     */
    fun reset() {
        _totalGCCount.set(0)
        _totalGCTime.set(0)
    }

    /**
     * Get current GC statistics from MXBeans
     * This provides an alternative to notification-based tracking
     */
    fun getCurrentStats(): Pair<Long, Long> {
        var count = 0L
        var time = 0L

        try {
            val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
            for (gcBean in gcBeans) {
                val gcCount = gcBean.collectionCount
                val gcTime = gcBean.collectionTime

                if (gcCount > 0) {
                    count += gcCount
                }
                if (gcTime > 0) {
                    time += gcTime
                }
            }
        } catch (e: Exception) {
            // Return current tracked values if MXBean access fails
        }

        return Pair(count, time)
    }
}
