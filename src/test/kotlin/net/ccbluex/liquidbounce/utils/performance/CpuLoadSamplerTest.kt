package net.ccbluex.liquidbounce.utils.performance

import org.junit.Assert.*
import org.junit.Test
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

class CpuLoadSamplerTest {
    @Test(timeout = 5000)
    fun slowCounterDoesNotBlockTheRequestingThread() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val finished = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val sampler = CpuLoadSampler(executor) {
                started.countDown()
                assertTrue(release.await(3, TimeUnit.SECONDS))
                CpuLoad(0.25, 0.75)
            }
            sampler.requestUpdate()
            assertTrue(started.await(2, TimeUnit.SECONDS))
            assertEquals(CpuLoad(), sampler.latest)
            repeat(100) { sampler.requestUpdate() }
            release.countDown()
            executor.execute { finished.countDown() }
            assertTrue(finished.await(2, TimeUnit.SECONDS))
            assertEquals(CpuLoad(0.25, 0.75), sampler.latest)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun requestsCoalesceUntilThePreviousSampleCompletes() {
        val queued = ArrayDeque<Runnable>()
        var calls = 0
        val sampler = CpuLoadSampler(Executor { queued.add(it) }) { calls++; CpuLoad(0.2, 0.3) }
        repeat(100) { sampler.requestUpdate() }
        assertEquals(1, queued.size)
        queued.remove().run()
        assertEquals(1, calls)
        sampler.requestUpdate()
        assertEquals(1, queued.size)
        queued.remove().run()
        assertEquals(2, calls)
    }

    @Test
    fun failedAndInvalidSamplesKeepPreviousValuesAndAllowRetry() {
        var read: () -> CpuLoad = { CpuLoad(0.2, 0.3) }
        val sampler = CpuLoadSampler(Executor { it.run() }) { read() }
        sampler.requestUpdate()
        read = { throw UnsupportedOperationException() }
        sampler.requestUpdate()
        assertEquals(CpuLoad(0.2, 0.3), sampler.latest)
        read = { CpuLoad(Double.NaN, -1.0) }
        sampler.requestUpdate()
        assertEquals(CpuLoad(0.2, 0.3), sampler.latest)
        read = { CpuLoad(1.1, Double.POSITIVE_INFINITY) }
        sampler.requestUpdate()
        assertEquals(CpuLoad(0.2, 0.3), sampler.latest)
        read = { CpuLoad(0.0, 1.0) }
        sampler.requestUpdate()
        assertEquals(CpuLoad(0.0, 1.0), sampler.latest)
    }

    @Test
    fun rejectedTaskCanBeRetried() {
        var reject = true
        val sampler = CpuLoadSampler(Executor {
            if (reject) throw RejectedExecutionException()
            it.run()
        }) { CpuLoad(0.5, 0.6) }
        sampler.requestUpdate()
        reject = false
        sampler.requestUpdate()
        assertEquals(CpuLoad(0.5, 0.6), sampler.latest)
    }
}
