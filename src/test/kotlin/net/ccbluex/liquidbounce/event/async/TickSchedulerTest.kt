/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.GameTickEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TickSchedulerTest {

    @Test
    fun `delayed task runs after the requested number of ticks`() {
        val requester = Any()
        var executions = 0

        try {
            assertTrue(TickScheduler.scheduleAfter(2, requester) { executions++ })
            assertTrue(TickScheduler.hasScheduled(requester))

            tick()
            assertEquals(0, executions)

            tick()
            assertEquals(1, executions)
            assertFalse(TickScheduler.hasScheduled(requester))
        } finally {
            TickScheduler.cancel(requester)
        }
    }

    @Test
    fun `conditional task stops on completion or timeout`() {
        val completedRequester = Any()
        val timedOutRequester = Any()
        val elapsed = mutableListOf<Int>()

        try {
            TickScheduler.scheduleConditional(completedRequester) {
                elapsed += it
                it >= 3
            }
            repeat(3) { tick() }

            assertEquals(listOf(1, 2, 3), elapsed)
            assertFalse(TickScheduler.hasScheduled(completedRequester))

            TickScheduler.scheduleConditional(timedOutRequester, maxTicks = 2) { null }
            tick()
            assertTrue(TickScheduler.hasScheduled(timedOutRequester))
            tick()
            assertFalse(TickScheduler.hasScheduled(timedOutRequester))
        } finally {
            TickScheduler.cancel(completedRequester)
            TickScheduler.cancel(timedOutRequester)
        }
    }

    @Test
    fun `cancellation prevents execution`() {
        val requester = Any()
        var executed = false

        TickScheduler.scheduleAfter(1, requester) { executed = true }
        assertEquals(1, TickScheduler.cancel(requester))

        tick()

        assertFalse(executed)
        assertFalse(TickScheduler.hasScheduled(requester))
    }

    @Test
    fun `cancelled tick suspension stops evaluating its condition`() = runBlocking {
        var evaluations = 0
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            waitUntil {
                evaluations++
                false
            }
        }

        tick()
        assertEquals(1, evaluations)

        job.cancelAndJoin()
        tick()
        assertEquals(1, evaluations)
    }

    @Test
    fun `conditional wait uses exact tick count and reports interruption`() = runBlocking {
        val elapsed = mutableListOf<Int>()
        val timeout = launch(start = CoroutineStart.UNDISPATCHED) {
            assertTrue(waitConditional(2) {
                elapsed += it
                false
            })
        }

        tick()
        assertTrue(timeout.isActive)
        tick()
        timeout.join()
        assertEquals(listOf(1, 2), elapsed)

        val interrupted = launch(start = CoroutineStart.UNDISPATCHED) {
            assertFalse(waitConditional(5) { it == 2 })
        }
        tick()
        tick()
        interrupted.join()
    }

    @Test
    fun `zero maximum conditional task does not execute`() {
        var executions = 0
        assertTrue(TickScheduler.scheduleConditional(maxTicks = 0) {
            executions++
            false
        })
        tick()
        assertEquals(0, executions)
    }

    private fun tick() {
        EventManager.call(GameTickEvent)
    }
}
