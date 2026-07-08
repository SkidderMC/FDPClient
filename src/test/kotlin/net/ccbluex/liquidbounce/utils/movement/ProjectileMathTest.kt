/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.minecraft.util.Vec3
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectileMathTest {

    @Test
    fun `point on segment has zero distance`() {
        assertEquals(0.0, distanceSqPointToSegment(vec(1.0, 0.0), vec(0.0, 0.0), vec(2.0, 0.0)), 0.0)
    }

    @Test
    fun `projection inside segment uses perpendicular distance`() {
        assertEquals(9.0, distanceSqPointToSegment(vec(1.0, 3.0), vec(0.0, 0.0), vec(2.0, 0.0)), 1e-12)
    }

    @Test
    fun `projection before segment clamps to start`() {
        assertEquals(2.0, distanceSqPointToSegment(vec(-1.0, 1.0), vec(0.0, 0.0), vec(2.0, 0.0)), 1e-12)
    }

    @Test
    fun `projection after segment clamps to end`() {
        assertEquals(2.0, distanceSqPointToSegment(vec(3.0, 1.0), vec(0.0, 0.0), vec(2.0, 0.0)), 1e-12)
    }

    @Test
    fun `zero length segment becomes point distance`() {
        assertEquals(25.0, distanceSqPointToSegment(vec(3.0, 4.0), vec(0.0, 0.0), vec(0.0, 0.0)), 1e-12)
    }

    private fun vec(x: Double, y: Double) = Vec3(x, y, 0.0)
}
