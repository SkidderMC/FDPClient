/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JumpPhysicsTest {

    @Test
    fun `free fall applies gravity and vertical drag exactly once`() {
        assertEquals(-0.0784, nextVanillaAirMotionY(0.0), 1e-12)
    }

    @Test
    fun `vanilla jump does not clear two blocks`() {
        assertFalse(projectedVanillaJumpHeight(0.42) >= 2.0)
    }

    @Test
    fun `strong jump boost clears two blocks`() {
        assertTrue(projectedVanillaJumpHeight(0.62) >= 2.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non finite motion is rejected`() {
        projectedVanillaJumpHeight(Double.POSITIVE_INFINITY)
    }
}
