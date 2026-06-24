/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import kotlin.math.abs

object RotationVerification {

    @JvmStatic
    fun main(args: Array<String>) {
        verifyWrapping()
        verifySensitivityQuantization()
        verifyValidation()
        verifyRequestArbitration()
        verifyActionTiming()
        verifyModernSmoothing()

        println("Rotation verification passed")
    }

    private fun verifyWrapping() {
        assertClose(-180f, RotationMath.wrapDegrees(180f))
        assertClose(-180f, RotationMath.wrapDegrees(540f))
        assertClose(2f, RotationMath.angleDifference(-179f, 179f))
        assertClose(-2f, RotationMath.angleDifference(179f, -179f))
        assertClose(5f, RotationMath.rotationDifference(3f, 4f, 0f, 0f))
    }

    private fun verifySensitivityQuantization() {
        val gcd = RotationMath.fixedAngleDelta(0.5f)

        assertClose(0.15f, gcd)
        assertClose(1.05f, RotationMath.fixedSensitivityAngle(1f, 0f, gcd))
        assertClose(-1.05f, RotationMath.fixedSensitivityAngle(-1f, 0f, gcd))
    }

    private fun verifyValidation() {
        check(RotationMath.isValid(0f, -90f))
        check(RotationMath.isValid(360f, 90f))
        check(!RotationMath.isValid(Float.NaN, 0f))
        check(!RotationMath.isValid(0f, Float.POSITIVE_INFINITY))
        check(!RotationMath.isValid(0f, 90.01f))
    }

    private fun verifyRequestArbitration() {
        val arbiter = RotationRequestArbiter()
        val normalOwner = Any()
        val competingOwner = Any()

        check(arbiter.tryAcquire(normalOwner, RotationPriority.NORMAL.level))
        check(!arbiter.tryAcquire(competingOwner, RotationPriority.LOW.level))
        check(arbiter.tryAcquire(competingOwner, RotationPriority.NORMAL.level))
        check(!arbiter.release(normalOwner))
        check(arbiter.release(competingOwner))
        check(arbiter.activeRequest == null)

        check(arbiter.tryAcquire(normalOwner, RotationPriority.HIGH.level))
        check(!arbiter.canAcquire(competingOwner, RotationPriority.NORMAL.level))
        check(arbiter.canAcquire(competingOwner, RotationPriority.CRITICAL.level))

        arbiter.clear()
        check(arbiter.activeRequest == null)
    }

    private fun verifyActionTiming() {
        PostRotationExecutor.clear()
        val events = ArrayList<String>()

        val postMove = RotationMode(RotationActionTiming.POST_MOVE)
        check(postMove.execute({ events += "aim"; true }, { events += "action" }))
        check(events == listOf("aim"))
        val movementPacket = net.minecraft.network.play.client.C03PacketPlayer(true)
        PostRotationExecutor.markRotationPacket(movementPacket)
        PostRotationExecutor.onPacketSendCompleted(movementPacket)
        check(events == listOf("aim", "action"))

        events.clear()
        val afterAction = RotationMode(RotationActionTiming.INSTANT, aimAfterAction = true)
        check(afterAction.execute({ events += "aim"; true }, { events += "action" }))
        check(events == listOf("action", "aim"))

        events.clear()
        val rejected = RotationMode(RotationActionTiming.INSTANT)
        check(!rejected.execute({ events += "aim"; false }, { events += "action" }))
        check(events == listOf("aim"))
    }

    private fun verifyModernSmoothing() {
        // Sigmoid mode factor: logistic(scaledDifference, steepness, midpoint) * turnSpeed.
        assertClose(9.7069f, ModernRotationEngine.computeSigmoidFactor(120f, 10f, 5f, 0.3f), 1.0E-3f)
        assertClose(1.8243f, ModernRotationEngine.computeSigmoidFactor(0f, 10f, 5f, 0.3f), 1.0E-3f)

        // Quadratic bezier control curve used by interpolation smoothing (middle control point fixed at 1.0).
        assertClose(0.05f, ModernRotationEngine.bezier(0.05f, 1f, 0f))
        assertClose(0.7625f, ModernRotationEngine.bezier(0.05f, 1f, 0.5f))
        assertClose(1f, ModernRotationEngine.bezier(0.05f, 1f, 1f))

        // Logistic ramp used by interpolation smoothing.
        assertClose(0.5f, ModernRotationEngine.interpolationSigmoid(0.3f))
        assertClose(0.46257f, ModernRotationEngine.interpolationSigmoid(0f), 1.0E-3f)

        // Interpolation factor: bezier branch above the midpoint, logistic branch below.
        assertClose(0.05f, ModernRotationEngine.interpolationFactor(180f, 1f, 0f, 0.5f), 1.0E-3f)
        assertClose(0.46257f, ModernRotationEngine.interpolationFactor(0f, 1f, 0f, 0.5f), 1.0E-3f)
    }

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1.0E-4f) {
        check(abs(expected - actual) <= tolerance) {
            "Expected $expected, got $actual"
        }
    }
}
