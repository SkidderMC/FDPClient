/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations
import net.ccbluex.liquidbounce.utils.extensions.random
import net.ccbluex.liquidbounce.utils.extensions.withGCD
import kotlin.math.abs

// TODO: refactor them all

class AlwaysRotationSettings(owner: Module, generalApply: () -> Boolean = { true }) :
    RotationSettings(owner, generalApply) {
    override val rotationsValue = super.rotationsValue.apply { excludeWithState(true) }
    override val rotationsActive: Boolean = true
}

@Suppress("MemberVisibilityCanBePrivate")
open class RotationSettings(val moduleOwner: Module, generalApply: () -> Boolean = { true }) : Configurable("RotationSettings") {

    private lateinit var flattenedValues: List<net.ccbluex.liquidbounce.config.Value<*>>

    open val rotationsValue = boolean("Rotations", true) { generalApply() }
    open val applyServerSideValue = boolean("ApplyServerSide", true) { rotationsActive && generalApply() }
    // Legacy-engine-only knobs hide while the Modern engine drives the aim, where their modern
    // counterparts (AngleSmooth/TurnSpeed/TicksUntilReset/ShortStop/MovementCorrection) apply instead.
    open val simulateShortStopValue = boolean("SimulateShortStop", false) {
        rotationsActive && !useModernRotations && generalApply()
    }
    open val rotationDiffBuildUpToStopValue = float("RotationDiffBuildUpToStop", 180f, 50f..720f) { simulateShortStop }
    open val maxThresholdAttemptsToStopValue = int("MaxThresholdAttemptsToStop", 1, 0..5) { simulateShortStop }
    // Shared by the legacy SimulateShortStop build-up and the modern ShortStop processor.
    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) { simulateShortStop || modernShortStop }
    open val strafeValue = boolean("Strafe", false) {
        rotationsActive && applyServerSide && !useModernRotations && generalApply()
    }
    open val strictValue = boolean("Strict", false) { strafeValue.isActive() && generalApply() }
    open val keepRotationValue = boolean("KeepRotation", true) { rotationsActive && applyServerSide && generalApply() }

    open val resetTicksValue = int("ResetTicks", 1, 1..20) {
        rotationsActive && applyServerSide && !useModernRotations && generalApply()
    }

    open val legitimizeValue = boolean("Legitimize", false) {
        rotationsActive && !useModernRotations && generalApply()
    }

    open val horizontalAngleChangeValue =
        floatRange("HorizontalAngleChange", 180f..180f, 1f..180f) {
            rotationsActive && !useModernRotations && generalApply()
        }
    open val verticalAngleChangeValue =
        floatRange("VerticalAngleChange", 180f..180f, 1f..180f) {
            rotationsActive && !useModernRotations && generalApply()
        }

    open val angleResetDifferenceValue = float("AngleResetDifference", 5f.withGCD(), 0.0f..180f) {
        rotationsActive && applyServerSide && !useModernRotations && generalApply()
    }

    open val minRotationDifferenceValue = float(
        "MinRotationDifference", 2f, 0f..4f
    ) { rotationsActive && !useModernRotations && generalApply() }

    open val maximumRotationDifferenceValue = float(
        "MaximumRotationDifference", 180f, 1f..180f, "°"
    ) { rotationsActive && generalApply() }

    open val minRotationDifferenceResetTimingValue = choices(
        "MinRotationDifferenceResetTiming", arrayOf("OnStart", "OnSlowDown", "Always"), "OnStart"
    ) { rotationsActive && !useModernRotations && generalApply() }

    open val rotationEngineValue = choices(
        "Engine", arrayOf("Legacy", "Modern"), "Legacy"
    ) { rotationsActive && generalApply() }

    open val modernAngleSmoothValue = choices(
        "AngleSmooth", arrayOf("None", "Linear", "Sigmoid", "Interpolation", "Acceleration", "AI"), "Linear"
    ) { useModernRotations && generalApply() }

    open val modernMovementCorrectionValue = choices(
        "MovementCorrection", arrayOf("Off", "Strict", "Silent", "ChangeLook"), "Silent"
    ) { useModernRotations && applyServerSide && generalApply() }

    open val modernResetThresholdValue = float("ResetThreshold", 2f, 1f..180f) {
        useModernRotations && applyServerSide && generalApply()
    }

    open val modernTicksUntilResetValue = int("TicksUntilReset", 5, 1..30, "ticks") {
        useModernRotations && applyServerSide && generalApply()
    }

    open val modernHorizontalTurnSpeedValue = floatRange(
        "HorizontalTurnSpeed", 180f..180f, 0f..180f
    ) { useModernRotations && modernAngleSmooth in arrayOf("Linear", "Sigmoid") && generalApply() }

    open val modernVerticalTurnSpeedValue = floatRange(
        "VerticalTurnSpeed", 180f..180f, 0f..180f
    ) { useModernRotations && modernAngleSmooth in arrayOf("Linear", "Sigmoid") && generalApply() }

    open val modernSigmoidSteepnessValue = float("SigmoidSteepness", 10f, 0f..20f) {
        useModernRotations && modernAngleSmooth == "Sigmoid" && generalApply()
    }

    open val modernSigmoidMidpointValue = float("SigmoidMidpoint", 0.3f, 0f..1f) {
        useModernRotations && modernAngleSmooth == "Sigmoid" && generalApply()
    }

    open val modernInterpolationHorizontalSpeedValue = intRange(
        "InterpolationHorizontalSpeed", 80..85, 1..100, "%"
    ) { useModernRotations && modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationVerticalSpeedValue = intRange(
        "InterpolationVerticalSpeed", 20..25, 1..100, "%"
    ) { useModernRotations && modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationDirectionChangeFactorValue = intRange(
        "InterpolationDirectionChangeFactor", 95..100, 0..100, "%"
    ) { useModernRotations && modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationMidpointValue = float("InterpolationMidpoint", 0.35f, 0f..1f) {
        useModernRotations && modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply()
    }

    open val modernAiOutputMultiplierValue = float("AIOutputMultiplier", 1f, 0.1f..2f) {
        useModernRotations && modernAngleSmooth == "AI" && generalApply()
    }

    open val modernYawAccelerationValue = floatRange(
        "YawAcceleration", 20f..25f, 1f..180f
    ) { useModernRotations && modernAngleSmooth == "Acceleration" && generalApply() }

    open val modernPitchAccelerationValue = floatRange(
        "PitchAcceleration", 20f..25f, 1f..180f
    ) { useModernRotations && modernAngleSmooth == "Acceleration" && generalApply() }

    open val modernAccelerationErrorValue = boolean("AccelerationError", true) {
        useModernRotations && modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernYawAccelerationErrorValue = float("YawAccelError", 0.1f, 0.01f..1f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernAccelerationError && generalApply()
    }

    open val modernPitchAccelerationErrorValue = float("PitchAccelError", 0.1f, 0.01f..1f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernAccelerationError && generalApply()
    }

    open val modernConstantErrorValue = boolean("ConstantError", true) {
        useModernRotations && modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernYawConstantErrorValue = float("YawConstantError", 0.1f, 0.01f..1f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernConstantError && generalApply()
    }

    open val modernPitchConstantErrorValue = float("PitchConstantError", 0.1f, 0.01f..1f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernConstantError && generalApply()
    }

    open val modernSigmoidDecelerationValue = boolean("SigmoidDeceleration", false) {
        useModernRotations && modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernSigmoidDecelerationSteepnessValue = float("DecelerationSteepness", 10f, 0f..20f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernSigmoidDeceleration && generalApply()
    }

    open val modernSigmoidDecelerationMidpointValue = float("DecelerationMidpoint", 0.3f, 0f..1f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernSigmoidDeceleration && generalApply()
    }

    open val modernDynamicAccelValue = boolean("DynamicAccel", false) {
        useModernRotations && modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernDynamicAccelCoefValue = float("CoefDistance", -1.393f, -2f..2f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernYawCrosshairAccelValue = floatRange("YawCrosshairAccel", 17f..20f, 1f..180f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernPitchCrosshairAccelValue = floatRange("PitchCrosshairAccel", 17f..20f, 1f..180f) {
        useModernRotations && modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernShortStopValue = boolean("ShortStop", false) {
        useModernRotations && generalApply()
    }

    open val modernShortStopRateValue = int("ShortStopRate", 3, 1..25, "%") {
        useModernRotations && modernShortStop && generalApply()
    }

    open val modernFailValue = boolean("Fail", false) {
        useModernRotations && generalApply()
    }

    open val modernFailRateValue = int("FailRate", 3, 1..100, "%") {
        useModernRotations && modernFail && generalApply()
    }

    open val modernFailFactorValue = float("FailFactor", 0.04f, 0.01f..0.99f) {
        useModernRotations && modernFail && generalApply()
    }

    open val modernFailStrengthHorizontalValue = floatRange("FailStrengthHorizontal", 5f..10f, 1f..90f, "deg") {
        useModernRotations && modernFail && generalApply()
    }

    open val modernFailStrengthVerticalValue = floatRange("FailStrengthVertical", 0f..2f, 0f..90f, "deg") {
        useModernRotations && modernFail && generalApply()
    }

    open val modernFailTransitionDurationValue = intRange("FailTransitionInDuration", 1..4, 0..20, "ticks") {
        useModernRotations && modernFail && generalApply()
    }

    // Variables for easier access
    val rotations by rotationsValue
    val applyServerSide by applyServerSideValue
    val simulateShortStop by simulateShortStopValue
    val rotationDiffBuildUpToStop by rotationDiffBuildUpToStopValue
    val maxThresholdAttemptsToStop by maxThresholdAttemptsToStopValue
    val shortStopDuration by shortStopDurationValue
    val strafe by strafeValue
    val strict by strictValue
    val keepRotation by keepRotationValue
    val resetTicks by resetTicksValue
    val legitimize by legitimizeValue
    val horizontalAngleChange by horizontalAngleChangeValue
    val verticalAngleChange by verticalAngleChangeValue
    val angleResetDifference by angleResetDifferenceValue
    val minRotationDifference by minRotationDifferenceValue
    val maximumRotationDifference by maximumRotationDifferenceValue
    val minRotationDifferenceResetTiming by minRotationDifferenceResetTimingValue
    val rotationEngine by rotationEngineValue
    val modernAngleSmooth by modernAngleSmoothValue
    val modernMovementCorrection by modernMovementCorrectionValue
    val modernResetThreshold by modernResetThresholdValue
    val modernTicksUntilReset by modernTicksUntilResetValue
    val modernHorizontalTurnSpeed by modernHorizontalTurnSpeedValue
    val modernVerticalTurnSpeed by modernVerticalTurnSpeedValue
    val modernSigmoidSteepness by modernSigmoidSteepnessValue
    val modernSigmoidMidpoint by modernSigmoidMidpointValue
    val modernInterpolationHorizontalSpeed by modernInterpolationHorizontalSpeedValue
    val modernInterpolationVerticalSpeed by modernInterpolationVerticalSpeedValue
    val modernInterpolationDirectionChangeFactor by modernInterpolationDirectionChangeFactorValue
    val modernInterpolationMidpoint by modernInterpolationMidpointValue
    val modernAiOutputMultiplier by modernAiOutputMultiplierValue
    val modernYawAcceleration by modernYawAccelerationValue
    val modernPitchAcceleration by modernPitchAccelerationValue
    val modernAccelerationError by modernAccelerationErrorValue
    val modernYawAccelerationError by modernYawAccelerationErrorValue
    val modernPitchAccelerationError by modernPitchAccelerationErrorValue
    val modernConstantError by modernConstantErrorValue
    val modernYawConstantError by modernYawConstantErrorValue
    val modernPitchConstantError by modernPitchConstantErrorValue
    val modernSigmoidDeceleration by modernSigmoidDecelerationValue
    val modernSigmoidDecelerationSteepness by modernSigmoidDecelerationSteepnessValue
    val modernSigmoidDecelerationMidpoint by modernSigmoidDecelerationMidpointValue
    val modernDynamicAccel by modernDynamicAccelValue
    val modernDynamicAccelCoef by modernDynamicAccelCoefValue
    val modernYawCrosshairAccel by modernYawCrosshairAccelValue
    val modernPitchCrosshairAccel by modernPitchCrosshairAccelValue
    val modernShortStop by modernShortStopValue
    val modernShortStopRate by modernShortStopRateValue
    val modernFail by modernFailValue
    val modernFailRate by modernFailRateValue
    val modernFailFactor by modernFailFactorValue
    val modernFailStrengthHorizontal by modernFailStrengthHorizontalValue
    val modernFailStrengthVertical by modernFailStrengthVerticalValue
    val modernFailTransitionDuration by modernFailTransitionDurationValue

    var prioritizeRequest = false
    var requestPriority = RotationPriority.NORMAL
    var immediate = false
    var instant = false

    var rotDiffBuildUp = 0f
    var maxThresholdReachAttempts = 0

    open val rotationsActive
        get() = rotations

    val useModernRotations
        get() = when (Rotations.engine) {
            "Modern" -> true
            "Legacy" -> false
            else -> rotationEngine == "Modern"
        }

    val effectiveResetTicks
        get() = if (useModernRotations) modernTicksUntilReset else resetTicks

    val effectiveRequestPriority
        get() = if (prioritizeRequest) RotationPriority.CRITICAL.level else requestPriority.level

    val horizontalSpeed
        get() = horizontalAngleChange.random()

    val verticalSpeed
        get() = verticalAngleChange.random()

    fun withoutKeepRotation() = apply {
        keepRotationValue.excludeWithState()
    }

    fun withRequestPriority(priority: RotationPriority) = apply {
        requestPriority = priority
    }

    /** Restores this legacy-flat settings bundle as a real nested configurable. */
    fun nestInto(parent: Configurable) = apply {
        addValues(flattenedValues)
        parent.addValue(this)
    }

    fun updateSimulateShortStopData(diff: Float) {
        rotDiffBuildUp += diff
    }

    fun resetSimulateShortStopData() {
        rotDiffBuildUp = 0f
        maxThresholdReachAttempts = 0
    }

    fun shouldPerformShortStop(): Boolean {
        if (abs(rotDiffBuildUp) < rotationDiffBuildUpToStop || !simulateShortStop) return false

        if (maxThresholdReachAttempts < maxThresholdAttemptsToStop) {
            maxThresholdReachAttempts++
            return false
        }

        return true
    }

    init {
        flattenedValues = values.toList()
        moduleOwner.addValues(flattenedValues)
    }
}

class RotationSettingsWithRotationModes(
    owner: Module, listValue: ListValue, generalApply: () -> Boolean = { true },
) : RotationSettings(owner, generalApply) {

    override val rotationsValue = super.rotationsValue.apply { excludeWithState() }

    val rotationModeValue = listValue.setSupport { generalApply() }

    val rotationMode by +rotationModeValue

    var rotationModeProvider: (() -> String)? = null
    var rotationsActiveProvider: (() -> Boolean)? = null

    val activeRotationMode: String
        get() = rotationModeProvider?.invoke() ?: rotationMode

    override val rotationsActive: Boolean
        get() = (rotationsActiveProvider?.invoke() ?: activeRotationMode) != "Off"
}
