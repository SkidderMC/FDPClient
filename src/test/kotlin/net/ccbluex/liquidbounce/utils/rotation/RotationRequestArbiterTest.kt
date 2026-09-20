package net.ccbluex.liquidbounce.utils.rotation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationRequestArbiterTest {

    @Test
    fun `lower priority cannot replace active owner`() {
        val arbiter = RotationRequestArbiter()
        val owner = Any()
        val competitor = Any()

        assertTrue(arbiter.tryAcquire(owner, RotationPriority.HIGH.level))
        assertFalse(arbiter.tryAcquire(competitor, RotationPriority.NORMAL.level))
        assertSame(owner, arbiter.activeRequest?.owner)
    }

    @Test
    fun `equal priority keeps last writer compatibility`() {
        val arbiter = RotationRequestArbiter()
        val first = Any()
        val second = Any()

        assertTrue(arbiter.tryAcquire(first, RotationPriority.NORMAL.level))
        assertTrue(arbiter.tryAcquire(second, RotationPriority.NORMAL.level))
        assertSame(second, arbiter.activeRequest?.owner)
    }

    @Test
    fun `owner refresh replaces lease and resets idle ticks`() {
        val arbiter = RotationRequestArbiter()
        val owner = Any()

        arbiter.tryAcquire(owner, RotationPriority.NORMAL.level)
        arbiter.tick(maxIdleTicks = 10)
        val firstSequence = arbiter.activeRequest!!.sequence
        assertTrue(arbiter.activeRequest!!.idleTicks > 0)

        assertTrue(arbiter.tryAcquire(owner, RotationPriority.NORMAL.level))
        assertTrue(arbiter.activeRequest!!.sequence > firstSequence)
        assertTrue(arbiter.activeRequest!!.idleTicks == 0)
    }

    @Test
    fun `stale lease expires only after configured idle budget`() {
        val arbiter = RotationRequestArbiter()
        val owner = Any()

        arbiter.tryAcquire(owner, RotationPriority.CRITICAL.level)
        repeat(3) { arbiter.tick(maxIdleTicks = 3) }
        assertSame(owner, arbiter.activeRequest?.owner)

        arbiter.tick(maxIdleTicks = 3)
        assertNull(arbiter.activeRequest)
    }

    @Test
    fun `only active owner can release lease`() {
        val arbiter = RotationRequestArbiter()
        val owner = Any()

        arbiter.tryAcquire(owner, RotationPriority.NORMAL.level)
        assertFalse(arbiter.release(Any()))
        assertTrue(arbiter.release(owner))
        assertNull(arbiter.activeRequest)
    }
}
