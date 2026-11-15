package net.ccbluex.liquidbounce.utils.performance

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

internal data class CpuLoad(val process: Double = 0.0, val system: Double = 0.0)

/** OS counters can block on Windows. Readers only consume the last completed sample. */
internal class CpuLoadSampler(private val executor: Executor, private val readLoad: () -> CpuLoad) {
    private val pending = AtomicBoolean()

    @Volatile
    var latest = CpuLoad()
        private set

    fun requestUpdate() {
        if (!pending.compareAndSet(false, true)) return
        try {
            executor.execute {
                try {
                    val next = readLoad()
                    val previous = latest
                    latest = CpuLoad(
                        next.process.takeIf { it.isFinite() && it in 0.0..1.0 } ?: previous.process,
                        next.system.takeIf { it.isFinite() && it in 0.0..1.0 } ?: previous.system
                    )
                } catch (_: Exception) {
                    // Unsupported or temporarily unavailable counters keep the last sample.
                } finally {
                    pending.set(false)
                }
            }
        } catch (_: RejectedExecutionException) {
            pending.set(false)
        }
    }
}
