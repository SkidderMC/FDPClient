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

class RotationMathTest {

    @Test
    fun `legacy sensitivity uses float precision`() {
        val sensitivity = 0.5f
        val factor = sensitivity * 0.6f + 0.2f
        assertEquals(factor * factor * factor * 1.2f, RotationMath.fixedAngleDelta(sensitivity), 0f)
    }

    @Test
    fun `degree wrapping is stable at boundaries`() {
        assertEquals(-180f, RotationMath.wrapDegrees(180f), 0f)
        assertEquals(179f, RotationMath.wrapDegrees(-181f), 0f)
    }

    @Test
    fun `angle difference crosses wrap boundary on shortest path`() {
        assertEquals(2f, RotationMath.angleDifference(-179f, 179f), 0f)
    }

    @Test
    fun `sensitivity quantization anchors to server rotation`() {
        assertEquals(10.3f, RotationMath.fixedSensitivityAngle(10.26f, 10f, 0.1f), 1e-6f)
    }

    @Test
    fun `rotation validation rejects non finite and invalid pitch`() {
        assertTrue(RotationMath.isValid(360f, 90f))
        assertFalse(RotationMath.isValid(Float.NaN, 0f))
        assertFalse(RotationMath.isValid(0f, Float.POSITIVE_INFINITY))
        assertFalse(RotationMath.isValid(0f, 90.01f))
    }
}
