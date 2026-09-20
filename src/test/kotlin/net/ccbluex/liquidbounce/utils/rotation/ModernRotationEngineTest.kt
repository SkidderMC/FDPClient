package net.ccbluex.liquidbounce.utils.rotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModernRotationEngineTest {

    @Test
    fun `sigmoid factor grows with angular error and stays bounded`() {
        val near = ModernRotationEngine.computeSigmoidFactor(0f, 10f, 5f, 0.3f)
        val far = ModernRotationEngine.computeSigmoidFactor(120f, 10f, 5f, 0.3f)

        assertTrue(near in 0f..10f)
        assertTrue(far in 0f..10f)
        assertTrue(far > near)
    }

    @Test
    fun `bezier interpolation preserves endpoints`() {
        assertEquals(0.05f, ModernRotationEngine.bezier(0.05f, 1f, 0f), 1.0E-5f)
        assertEquals(1f, ModernRotationEngine.bezier(0.05f, 1f, 1f), 1.0E-5f)
    }

    @Test
    fun `neural factor clamps hostile inputs and output`() {
        val factor = ModernRotationEngine.neuralFactor(
            error = Float.POSITIVE_INFINITY,
            momentum = -10f,
            directionChange = 20f,
            targetDistance = -4f,
            targetMotion = 8f,
            baseSpeed = 99f,
            outputMultiplier = 99f,
        )

        assertTrue(factor in 0.005f..1f)
        assertTrue(factor.isFinite())
    }

    @Test
    fun `reset clears temporal smoothing state`() {
        ModernRotationEngine.reset()

        assertEquals(null, ModernRotationEngine.previousRotation)
        assertEquals(null, ModernRotationEngine.previousTargetRotation)
    }
}
