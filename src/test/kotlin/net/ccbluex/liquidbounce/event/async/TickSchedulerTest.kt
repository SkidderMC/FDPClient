/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

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

    private fun tick() {
        EventManager.call(GameTickEvent)
    }
}
