/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * Pure rotation math shared by the runtime manager and deterministic verification tasks.
 */
object RotationMath {

    fun wrapDegrees(angle: Float): Float {
        var wrapped = angle % 360f

        if (wrapped >= 180f) wrapped -= 360f
        if (wrapped < -180f) wrapped += 360f

        return wrapped
    }

    fun angleDifference(target: Float, current: Float) = wrapDegrees(target - current)

    fun rotationDifference(
        targetYaw: Float,
        targetPitch: Float,
        currentYaw: Float,
        currentPitch: Float,
    ) = hypot(angleDifference(targetYaw, currentYaw), targetPitch - currentPitch)

    fun fixedAngleDelta(sensitivity: Float) = (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

    fun fixedSensitivityAngle(targetAngle: Float, startAngle: Float, gcd: Float) =
        startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

    fun isValid(yaw: Float, pitch: Float) =
        yaw.isFinite() && pitch.isFinite() && pitch in -90f..90f

    /**
     * Largest yaw step worth sending in one packet. A turn beyond this in a single tick is read as a
     * snap no mouse could produce, so a larger move is spread over the following ticks instead.
     */
    const val MAX_YAW_STEP = 300f

    /**
     * Two consecutive turns closer than this are treated as the same turn repeated. Machine-exact
     * repetition is what betrays a generated aim; a human never reproduces an angle this precisely.
     */
    const val REPEATED_DELTA_EPSILON = 0.001f

    /**
     * Limits how far the yaw may travel in a single step, keeping the winding continuous.
     */
    fun clampYawStep(fromYaw: Float, toYaw: Float, maxStep: Float = MAX_YAW_STEP): Float {
        val delta = toYaw - fromYaw

        if (!delta.isFinite() || abs(delta) <= maxStep) {
            return toYaw
        }

        return fromYaw + maxStep * sign(delta)
    }

    /**
     * Breaks an exact repeat of the previous turn by the smallest step a mouse can make.
     *
     * Only turns large enough to be measured are adjusted, and only when they would land within
     * [REPEATED_DELTA_EPSILON] of the previous one, so the nudge stays far below anything that could
     * move where the aim actually points.
     */
    fun varyRepeatedYaw(yaw: Float, previousYaw: Float, previousDelta: Float, gcd: Float): Float {
        val delta = yaw - previousYaw

        if (gcd <= 0f || !delta.isFinite() || abs(delta) <= 2f) {
            return yaw
        }

        if (abs(abs(delta) - abs(previousDelta)) >= REPEATED_DELTA_EPSILON) {
            return yaw
        }

        return yaw + gcd * sign(delta)
    }
}
