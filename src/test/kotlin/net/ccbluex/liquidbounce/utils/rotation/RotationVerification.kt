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

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1.0E-4f) {
        check(abs(expected - actual) <= tolerance) {
            "Expected $expected, got $actual"
        }
    }
}
