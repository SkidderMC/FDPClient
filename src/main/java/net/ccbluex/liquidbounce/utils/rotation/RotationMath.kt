/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

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
}
