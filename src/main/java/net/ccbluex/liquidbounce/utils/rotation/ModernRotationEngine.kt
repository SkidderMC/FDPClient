/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.random
import net.ccbluex.liquidbounce.utils.extensions.rotation
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Dynamic aiming pipeline for the Modern rotation engine.
 *
 * Keeps the modern smoothing processor order and formulas, but runs entirely on FDP's existing 1.8.9
 * [Rotation] and [RotationSettings] contracts.
 */
object ModernRotationEngine : MinecraftInstance {

    var previousRotation: Rotation? = null
        private set

    var previousTargetRotation: Rotation? = null
        private set

    private var shortStopTicksElapsed = 0
    private var shortStopDuration = 0

    private var failTicksElapsed = 0
    private var failTransitionDuration = 0
    private var failShiftRotation = Rotation.ZERO

    fun reset() {
        previousRotation = null
        previousTargetRotation = null
        shortStopTicksElapsed = 0
        shortStopDuration = 0
        failTicksElapsed = 0
        failTransitionDuration = 0
        failShiftRotation = Rotation.ZERO
    }

    fun process(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean,
    ): Rotation {
        if (settings.instant) {
            return targetRotation.fixedSensitivity()
        }

        val smoothedRotation = when (settings.modernAngleSmooth) {
            "None" -> targetRotation
            "Sigmoid" -> sigmoid(currentRotation, targetRotation, settings)
            "Interpolation" -> interpolation(currentRotation, targetRotation, settings, resetting)
            "Acceleration" -> acceleration(currentRotation, targetRotation, settings)
            "AI" -> ai(currentRotation, targetRotation, settings, resetting)
            else -> linear(currentRotation, targetRotation, settings)
        }

        val failedTarget = processFail(currentRotation, smoothedRotation, settings, resetting)
        val finalRotation = processShortStop(currentRotation, failedTarget, settings, resetting)
            .withLimitedPitch()
            .fixedSensitivity()

        previousRotation = currentRotation
        previousTargetRotation = targetRotation

        return finalRotation
    }

    fun calculateTicks(currentRotation: Rotation, targetRotation: Rotation, settings: RotationSettings): Int {
        val snapshot = EngineState(
            previousRotation,
            previousTargetRotation,
            shortStopTicksElapsed,
            shortStopDuration,
            failTicksElapsed,
            failTransitionDuration,
            failShiftRotation,
        )
        return try {
            var rotation = Rotation(currentRotation.yaw, currentRotation.pitch)
            var ticks = -1
            do {
                rotation = process(rotation, targetRotation, settings, resetting = false)
                ticks++
            } while (!rotation.approximatelyEquals(targetRotation) && ticks < 80)
            ticks
        } finally {
            previousRotation = snapshot.previousRotation
            previousTargetRotation = snapshot.previousTargetRotation
            shortStopTicksElapsed = snapshot.shortStopTicksElapsed
            shortStopDuration = snapshot.shortStopDuration
            failTicksElapsed = snapshot.failTicksElapsed
            failTransitionDuration = snapshot.failTransitionDuration
            failShiftRotation = snapshot.failShiftRotation
        }
    }

    private fun linear(currentRotation: Rotation, targetRotation: Rotation, settings: RotationSettings): Rotation {
        val horizontalFactor = settings.modernHorizontalTurnSpeed.random()
        val verticalFactor = settings.modernVerticalTurnSpeed.random()

        return currentRotation.towardsLinear(targetRotation, horizontalFactor, verticalFactor)
    }

    private fun sigmoid(currentRotation: Rotation, targetRotation: Rotation, settings: RotationSettings): Rotation {
        val rotationDifference = currentRotation.angleTo(targetRotation)
        val horizontalFactor = computeSigmoidFactor(
            rotationDifference,
            settings.modernHorizontalTurnSpeed.random(),
            settings.modernSigmoidSteepness,
            settings.modernSigmoidMidpoint
        )
        val verticalFactor = computeSigmoidFactor(
            rotationDifference,
            settings.modernVerticalTurnSpeed.random(),
            settings.modernSigmoidSteepness,
            settings.modernSigmoidMidpoint
        )

        return currentRotation.towardsLinear(targetRotation, horizontalFactor, verticalFactor)
    }

    private fun interpolation(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean,
    ): Rotation {
        val delta = currentRotation.rotationDeltaTo(targetRotation)
        val directionChange = previousTargetRotation.takeIf { !resetting }?.let {
            normalizeDirectionChange(it.angleTo(targetRotation)) *
                (settings.modernInterpolationDirectionChangeFactor.random().toFloat() / 100f)
        } ?: 0f

        val horizontalSpeed = settings.modernInterpolationHorizontalSpeed.random().toFloat() / 100f
        val verticalSpeed = settings.modernInterpolationVerticalSpeed.random().toFloat() / 100f

        val horizontalFactor = interpolationFactor(
            abs(delta.deltaYaw),
            horizontalSpeed.coerceIn(0f, 1f),
            directionChange,
            settings.modernInterpolationMidpoint
        ) * abs(delta.deltaYaw)
        val verticalFactor = interpolationFactor(
            abs(delta.deltaPitch),
            verticalSpeed.coerceIn(0f, 1f),
            directionChange,
            settings.modernInterpolationMidpoint
        ) * abs(delta.deltaPitch)

        return currentRotation.towardsLinear(targetRotation, horizontalFactor, verticalFactor)
    }

    private fun acceleration(currentRotation: Rotation, targetRotation: Rotation, settings: RotationSettings): Rotation {
        val previous = previousRotation ?: mc.thePlayer?.rotation ?: RotationUtils.serverRotation
        val previousDelta = previous.rotationDeltaTo(currentRotation)
        val delta = currentRotation.rotationDeltaTo(targetRotation)

        if (abs(delta.deltaYaw) <= 1.0E-4f && abs(delta.deltaPitch) <= 1.0E-4f) {
            return targetRotation
        }

        val decelerationFactor = if (settings.modernSigmoidDeceleration) {
            computeSigmoidFactor(
                delta.length(),
                1f,
                settings.modernSigmoidDecelerationSteepness,
                settings.modernSigmoidDecelerationMidpoint
            )
        } else {
            1f
        }

        // DynamicAccel: bias acceleration by distance to the target and tighten it once the crosshair is on.
        val dynamic = settings.modernDynamicAccel
        val aimEntity = if (dynamic) RotationUtils.aimTargetEntity else null
        val aimDistance = aimEntity?.let { mc.thePlayer?.getDistanceToEntity(it)?.toDouble() } ?: 0.0
        val distanceFactor = if (dynamic) (settings.modernDynamicAccelCoef * aimDistance).toFloat() else 0f
        // Crosshair check: raycast the target hitbox along the in-progress rotation within reach,
        // matching the reference look-at test instead of a flat angular-delta proxy.
        val onTarget = dynamic && aimEntity != null &&
            RotationUtils.isRotationFaced(aimEntity, maxOf(3.0, aimDistance), currentRotation)

        val yawAccelerationRange = if (onTarget) settings.modernYawCrosshairAccel else settings.modernYawAcceleration
        val pitchAccelerationRange = if (onTarget) settings.modernPitchCrosshairAccel else settings.modernPitchAcceleration

        val yawAcceleration = calculateAcceleration(
            delta.deltaYaw,
            previousDelta.deltaYaw,
            (-yawAccelerationRange.random() + distanceFactor)..(yawAccelerationRange.random() + distanceFactor),
            decelerationFactor
        )
        val pitchAcceleration = calculateAcceleration(
            delta.deltaPitch,
            previousDelta.deltaPitch,
            (-pitchAccelerationRange.random() + distanceFactor)..(pitchAccelerationRange.random() + distanceFactor),
            decelerationFactor
        )

        val yawError = settings.modernYawAccelerationError.takeIf { settings.modernAccelerationError } ?: 0f
        val pitchError = settings.modernPitchAccelerationError.takeIf { settings.modernAccelerationError } ?: 0f
        val yawConstantError = settings.modernYawConstantError.takeIf { settings.modernConstantError } ?: 0f
        val pitchConstantError = settings.modernPitchConstantError.takeIf { settings.modernConstantError } ?: 0f

        val yawStep = previousDelta.deltaYaw +
            yawAcceleration +
            yawAcceleration * (-yawError..yawError).random() +
            (-yawConstantError..yawConstantError).random()
        val pitchStep = previousDelta.deltaPitch +
            pitchAcceleration +
            pitchAcceleration * (-pitchError..pitchError).random() +
            (-pitchConstantError..pitchConstantError).random()

        return Rotation(
            currentRotation.yaw + yawStep.coerceIn(-abs(delta.deltaYaw), abs(delta.deltaYaw)),
            currentRotation.pitch + pitchStep.coerceIn(-abs(delta.deltaPitch), abs(delta.deltaPitch))
        )
    }

    /** Lightweight embedded MLP smoother, avoiding a native inference dependency on the Java 8 client. */
    private fun ai(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean,
    ): Rotation {
        val delta = currentRotation.rotationDeltaTo(targetRotation)
        val previous = previousRotation ?: currentRotation
        val previousDelta = previous.rotationDeltaTo(currentRotation)
        val directionChange = previousTargetRotation.takeIf { !resetting }
            ?.angleTo(targetRotation)?.div(180f)?.coerceIn(0f, 1f) ?: 0f
        val entity = RotationUtils.aimTargetEntity
        val targetDistance = entity?.let { mc.thePlayer?.getDistanceToEntity(it) }?.div(12f)?.coerceIn(0f, 1f) ?: 0f
        val targetMotion = entity?.let { hypot(it.motionX, it.motionZ).toFloat() }?.div(0.8f)?.coerceIn(0f, 1f) ?: 0f

        val yawFactor = neuralFactor(
            abs(delta.deltaYaw) / 180f,
            abs(previousDelta.deltaYaw) / 180f,
            directionChange,
            targetDistance,
            targetMotion,
            settings.modernInterpolationHorizontalSpeed.random().toFloat() / 100f,
            settings.modernAiOutputMultiplier,
        )
        val pitchFactor = neuralFactor(
            abs(delta.deltaPitch) / 90f,
            abs(previousDelta.deltaPitch) / 90f,
            directionChange,
            targetDistance,
            targetMotion,
            settings.modernInterpolationVerticalSpeed.random().toFloat() / 100f,
            settings.modernAiOutputMultiplier,
        )

        return currentRotation.towardsLinear(
            targetRotation,
            (abs(delta.deltaYaw) * yawFactor).coerceAtLeast(RotationUtils.getFixedAngleDelta()),
            (abs(delta.deltaPitch) * pitchFactor).coerceAtLeast(RotationUtils.getFixedAngleDelta()),
        )
    }

    /** Two-layer learned-curve approximation with normalized inputs and a bounded output. */
    internal fun neuralFactor(
        error: Float,
        momentum: Float,
        directionChange: Float,
        targetDistance: Float,
        targetMotion: Float,
        baseSpeed: Float,
        outputMultiplier: Float,
    ): Float {
        val e = error.coerceIn(0f, 1f)
        val m = momentum.coerceIn(0f, 1f)
        val c = directionChange.coerceIn(0f, 1f)
        val d = targetDistance.coerceIn(0f, 1f)
        val v = targetMotion.coerceIn(0f, 1f)

        val hidden0 = tanh((1.55f * e - 0.70f * m + 0.42f * c + 0.24f * v - 0.18f).toDouble())
        val hidden1 = tanh((0.82f * e + 0.72f * d + 0.38f * v - 0.31f).toDouble())
        val hidden2 = tanh((1.12f * c + 0.46f * v - 0.12f * m + 0.08f).toDouble())
        val hidden3 = tanh((0.78f - 0.92f * e - 0.36f * d + 0.22f * m).toDouble())
        val output = 1.0 / (1.0 + exp(-(0.92 * hidden0 + 0.48 * hidden1 + 0.23 * hidden2 - 0.16 * hidden3 - 0.08)))

        return ((0.04 + 0.86 * output) * baseSpeed.coerceIn(0.01f, 1f) * outputMultiplier)
            .toFloat()
            .coerceIn(0.005f, 1f)
    }

    private fun processShortStop(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean,
    ): Rotation {
        if (resetting || !settings.modernShortStop) {
            shortStopTicksElapsed = 0
            return targetRotation
        }

        // Roll the stop chance every tick, independent of the previous stop window, so the
        // stop frequency matches the reference processor instead of only re-arming after a stop ends.
        if (settings.modernShortStopRate > (0..100).random()) {
            shortStopDuration = settings.shortStopDuration.random()
            shortStopTicksElapsed = 0
        }

        return if (shortStopTicksElapsed < shortStopDuration) {
            shortStopTicksElapsed++
            currentRotation.towardsLinear(targetRotation, (0f..0.1f).random(), (0f..0.1f).random())
        } else {
            targetRotation
        }
    }

    private fun processFail(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean,
    ): Rotation {
        if (resetting || !settings.modernFail) {
            failTicksElapsed = 0
            return targetRotation
        }

        // Roll the fail chance every tick (matching the reference), not only once the previous
        // transition window has elapsed; a successful roll re-arms the shift and resets the window.
        if (settings.modernFailRate > (0..100).random()) {
            failTransitionDuration = settings.modernFailTransitionDuration.random()
            failShiftRotation = Rotation(
                if (Random.nextBoolean()) settings.modernFailStrengthHorizontal.random()
                else -settings.modernFailStrengthHorizontal.random(),
                if (Random.nextBoolean()) settings.modernFailStrengthVertical.random()
                else -settings.modernFailStrengthVertical.random()
            )
            failTicksElapsed = 0
        } else {
            failTicksElapsed++
        }

        return if (failTicksElapsed < failTransitionDuration) {
            val previous = previousRotation ?: currentRotation
            val serverRotation = RotationUtils.serverRotation
            val deltaYaw = (previous.yaw - serverRotation.yaw) * settings.modernFailFactor
            val deltaPitch = (previous.pitch - serverRotation.pitch) * settings.modernFailFactor

            Rotation(
                targetRotation.yaw + deltaYaw + failShiftRotation.yaw,
                targetRotation.pitch + deltaPitch + failShiftRotation.pitch
            )
        } else {
            targetRotation
        }
    }

    private fun calculateAcceleration(
        diff: Float,
        previousDiff: Float,
        acceleration: ClosedFloatingPointRange<Float>,
        decelerationFactor: Float,
    ) = RotationUtils.angleDifference(diff, previousDiff)
        .coerceIn(acceleration) *
        decelerationFactor

    internal fun computeSigmoidFactor(
        rotationDifference: Float,
        turnSpeed: Float,
        steepness: Float,
        midpoint: Float,
    ): Float {
        val scaledDifference = rotationDifference / 120f
        val sigmoid = 1 / (1 + exp((-steepness * (scaledDifference - midpoint)).toDouble()))

        return (sigmoid * turnSpeed).toFloat().coerceIn(0f, 180f)
    }

    private fun normalizeDirectionChange(angle: Float) = (angle / 180f).coerceIn(0f, 1f)

    internal fun interpolationSigmoid(t: Float): Float {
        return 1f / (1f + exp(-0.5f * (t - 0.3f)))
    }

    internal fun bezier(start: Float, end: Float, t: Float): Float {
        return (1f - t) * (1f - t) * start + 2f * (1f - t) * t + t * t * end
    }

    internal fun interpolationFactor(
        rotationDifference: Float,
        turnSpeed: Float,
        directionChange: Float,
        midpoint: Float,
    ): Float {
        val t = normalizeDirectionChange(rotationDifference)
        val bezierSpeed = bezier(0.05f, 1f, 1f - t)
        val sigmoidSpeed = interpolationSigmoid(t)

        return if (t > midpoint) {
            bezierSpeed * turnSpeed
        } else {
            sigmoidSpeed * (turnSpeed + directionChange).coerceIn(0f, 1f)
        }
    }

    private fun Rotation.towardsLinear(
        target: Rotation,
        horizontalFactor: Float,
        verticalFactor: Float,
    ): Rotation {
        val delta = rotationDeltaTo(target)

        val yawStep = delta.deltaYaw.coerceIn(-horizontalFactor, horizontalFactor)
        val pitchStep = delta.deltaPitch.coerceIn(-verticalFactor, verticalFactor)

        return Rotation(yaw + yawStep, pitch + pitchStep)
    }

    private fun Rotation.rotationDeltaTo(target: Rotation): RotationDelta {
        return RotationDelta(
            RotationUtils.angleDifference(target.yaw, yaw),
            target.pitch - pitch
        )
    }

    private fun Rotation.angleTo(other: Rotation): Float {
        val delta = rotationDeltaTo(other)
        return delta.length()
    }

    private fun Rotation.approximatelyEquals(other: Rotation): Boolean {
        return angleTo(other) <= RotationUtils.getFixedAngleDelta().coerceAtLeast(0.1f)
    }

    private data class RotationDelta(val deltaYaw: Float, val deltaPitch: Float) {
        fun length() = hypot(deltaYaw, deltaPitch)
    }

    private data class EngineState(
        val previousRotation: Rotation?,
        val previousTargetRotation: Rotation?,
        val shortStopTicksElapsed: Int,
        val shortStopDuration: Int,
        val failTicksElapsed: Int,
        val failTransitionDuration: Int,
        val failShiftRotation: Rotation,
    )
}
