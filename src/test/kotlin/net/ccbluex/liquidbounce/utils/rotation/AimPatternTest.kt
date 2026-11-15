/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The assertions mirror the conditions the server side actually tests, so a regression here shows up
 * as a failing build instead of as a flag in game.
 */
class AimPatternTest {

    private val gcd = RotationMath.fixedAngleDelta(0.5f)

    /** A turn beyond 320 degrees in one packet, while the yaw itself is inside a single turn. */
    private fun readsAsSnap(fromYaw: Float, toYaw: Float) =
        abs(toYaw) < 360f && abs(toYaw - fromYaw) > 320f

    /** Two placements whose turn is the same to four decimals, for turns over two degrees. */
    private fun readsAsRepeat(delta: Float, previousDelta: Float) =
        abs(delta) > 2f && abs(abs(delta) - abs(previousDelta)) < 0.0001f

    @Test
    fun `a turn that would read as a snap is spread instead`() {
        val sent = RotationMath.clampYawStep(10f, 350f)

        assertFalse("sent ${sent - 10f} degrees in one step", readsAsSnap(10f, sent))
        assertEquals(10f + RotationMath.MAX_YAW_STEP, sent, 0f)
    }

    @Test
    fun `ordinary turns are left untouched`() {
        assertEquals(93.5f, RotationMath.clampYawStep(0f, 93.5f), 0f)
        assertEquals(-120f, RotationMath.clampYawStep(0f, -120f), 0f)
        assertEquals(0.15f, RotationMath.clampYawStep(0f, 0.15f), 0f)
    }

    @Test
    fun `clamping keeps the direction of the turn`() {
        assertTrue(RotationMath.clampYawStep(0f, 340f) > 0f)
        assertTrue(RotationMath.clampYawStep(0f, -340f) < 0f)
    }

    @Test
    fun `a repeated turn is broken by one sensitivity step`() {
        val previousDelta = 90f
        val yaw = RotationMath.varyRepeatedYaw(90f, 0f, previousDelta, gcd)

        assertFalse("turn repeated exactly", readsAsRepeat(yaw - 0f, previousDelta))
        assertEquals(90f + gcd, yaw, 1e-4f)
    }

    @Test
    fun `a turn that already differs is left alone`() {
        val yaw = RotationMath.varyRepeatedYaw(90f, 0f, 73f, gcd)

        assertEquals(90f, yaw, 0f)
    }

    @Test
    fun `small turns are never touched, as the check ignores them`() {
        assertEquals(1.5f, RotationMath.varyRepeatedYaw(1.5f, 0f, 1.5f, gcd), 0f)
        assertEquals(2f, RotationMath.varyRepeatedYaw(2f, 0f, 2f, gcd), 0f)
    }

    @Test
    fun `repeating the same turn many times never produces two equal deltas`() {
        var yaw = 0f
        var previousDelta = 0f
        var worstDiff = Float.MAX_VALUE

        repeat(200) {
            val wanted = yaw + 90f
            val sent = RotationMath.varyRepeatedYaw(wanted, yaw, previousDelta, gcd)
            val delta = sent - yaw

            if (previousDelta != 0f) {
                worstDiff = minOf(worstDiff, abs(abs(delta) - abs(previousDelta)))
            }

            previousDelta = delta
            yaw = sent
        }

        assertTrue("two placements repeated the same turn (closest $worstDiff)", worstDiff >= 0.0001f)
    }

    @Test
    fun `the nudge stays far below what could move the aim off a block`() {
        val yaw = RotationMath.varyRepeatedYaw(90f, 0f, 90f, gcd)

        assertTrue("nudge of ${yaw - 90f} is too large to be safe", abs(yaw - 90f) < 0.5f)
    }
}
