/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.features.module.Module

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
    open val keepRotationValue = boolean("KeepRotation", true) { rotationsActive && applyServerSide && generalApply() }

    open val maximumRotationDifferenceValue = float(
        "MaximumRotationDifference", 180f, 1f..180f, "°"
    ) { rotationsActive && generalApply() }

    open val modernAngleSmoothValue = choices(
        "AngleSmooth", arrayOf("None", "Linear", "Sigmoid", "Interpolation", "Acceleration", "AI"), "Linear"
    ) { rotationsActive && generalApply() }

    open val modernMovementCorrectionValue = choices(
        "MovementCorrection", arrayOf("Off", "Strict", "Silent", "ChangeLook"), "Silent"
    ) { rotationsActive && applyServerSide && generalApply() }

    open val modernResetThresholdValue = float("ResetThreshold", 2f, 1f..180f) {
        rotationsActive && applyServerSide && generalApply()
    }.apply { aliases("AngleResetDifference") }

    open val modernTicksUntilResetValue = int("TicksUntilReset", 5, 1..30, "ticks") {
        rotationsActive && applyServerSide && generalApply()
    }.apply { aliases("ResetTicks") }

    open val ignoreOpenInventoryValue = boolean("IgnoreOpenInventory", true) {
        rotationsActive && generalApply()
    }

    open val modernHorizontalTurnSpeedValue = floatRange(
        "HorizontalTurnSpeed", 180f..180f, 0f..180f
    ) { modernAngleSmooth in arrayOf("Linear", "Sigmoid") && generalApply() }
        .apply { aliases("HorizontalAngleChange") }

    open val modernVerticalTurnSpeedValue = floatRange(
        "VerticalTurnSpeed", 180f..180f, 0f..180f
    ) { modernAngleSmooth in arrayOf("Linear", "Sigmoid") && generalApply() }
        .apply { aliases("VerticalAngleChange") }

    open val modernSigmoidSteepnessValue = float("SigmoidSteepness", 10f, 0f..20f) {
        modernAngleSmooth == "Sigmoid" && generalApply()
    }

    open val modernSigmoidMidpointValue = float("SigmoidMidpoint", 0.3f, 0f..1f) {
        modernAngleSmooth == "Sigmoid" && generalApply()
    }

    open val modernInterpolationHorizontalSpeedValue = intRange(
        "InterpolationHorizontalSpeed", 80..85, 1..100, "%"
    ) { modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationVerticalSpeedValue = intRange(
        "InterpolationVerticalSpeed", 20..25, 1..100, "%"
    ) { modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationDirectionChangeFactorValue = intRange(
        "InterpolationDirectionChangeFactor", 95..100, 0..100, "%"
    ) { modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply() }

    open val modernInterpolationMidpointValue = float("InterpolationMidpoint", 0.35f, 0f..1f) {
        modernAngleSmooth in arrayOf("Interpolation", "AI") && generalApply()
    }

    open val modernAiYawMultiplierValue = float("AIYawMultiplier", 1.5f, 0.5f..2f) {
        modernAngleSmooth == "AI" && generalApply()
    }

    open val modernAiPitchMultiplierValue = float("AIPitchMultiplier", 1f, 0.5f..2f) {
        modernAngleSmooth == "AI" && generalApply()
    }

    open val modernAiCorrectionValue = choices(
        "AICorrection", arrayOf("Interpolation", "Linear", "None"), "Interpolation"
    ) { modernAngleSmooth == "AI" && generalApply() }

    open val modernAiCorrectionHorizontalSpeedValue = intRange(
        "AICorrectionHorizontalSpeed", 2..5, 1..100, "%"
    ) { modernAngleSmooth == "AI" && modernAiCorrection == "Interpolation" && generalApply() }

    open val modernAiCorrectionVerticalSpeedValue = intRange(
        "AICorrectionVerticalSpeed", 2..5, 1..100, "%"
    ) { modernAngleSmooth == "AI" && modernAiCorrection == "Interpolation" && generalApply() }

    open val modernAiCorrectionDirectionChangeValue = intRange(
        "AICorrectionDirectionChange", 95..100, 0..100, "%"
    ) { modernAngleSmooth == "AI" && modernAiCorrection == "Interpolation" && generalApply() }

    open val modernAiCorrectionLinearHorizontalValue = floatRange(
        "AICorrectionLinearHorizontal", 5f..5f, 0f..180f
    ) { modernAngleSmooth == "AI" && modernAiCorrection == "Linear" && generalApply() }

    open val modernAiCorrectionLinearVerticalValue = floatRange(
        "AICorrectionLinearVertical", 5f..5f, 0f..180f
    ) { modernAngleSmooth == "AI" && modernAiCorrection == "Linear" && generalApply() }

    open val modernYawAccelerationValue = floatRange(
        "YawAcceleration", 20f..25f, 1f..180f
    ) { modernAngleSmooth == "Acceleration" && generalApply() }

    open val modernPitchAccelerationValue = floatRange(
        "PitchAcceleration", 20f..25f, 1f..180f
    ) { modernAngleSmooth == "Acceleration" && generalApply() }

    open val modernAccelerationErrorValue = boolean("AccelerationError", true) {
        modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernYawAccelerationErrorValue = float("YawAccelError", 0.1f, 0.01f..1f) {
        modernAngleSmooth == "Acceleration" && modernAccelerationError && generalApply()
    }

    open val modernPitchAccelerationErrorValue = float("PitchAccelError", 0.1f, 0.01f..1f) {
        modernAngleSmooth == "Acceleration" && modernAccelerationError && generalApply()
    }

    open val modernConstantErrorValue = boolean("ConstantError", true) {
        modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernYawConstantErrorValue = float("YawConstantError", 0.1f, 0.01f..1f) {
        modernAngleSmooth == "Acceleration" && modernConstantError && generalApply()
    }

    open val modernPitchConstantErrorValue = float("PitchConstantError", 0.1f, 0.01f..1f) {
        modernAngleSmooth == "Acceleration" && modernConstantError && generalApply()
    }

    open val modernSigmoidDecelerationValue = boolean("SigmoidDeceleration", false) {
        modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernSigmoidDecelerationSteepnessValue = float("DecelerationSteepness", 10f, 0f..20f) {
        modernAngleSmooth == "Acceleration" && modernSigmoidDeceleration && generalApply()
    }

    open val modernSigmoidDecelerationMidpointValue = float("DecelerationMidpoint", 0.3f, 0f..1f) {
        modernAngleSmooth == "Acceleration" && modernSigmoidDeceleration && generalApply()
    }

    open val modernDynamicAccelValue = boolean("DynamicAccel", false) {
        modernAngleSmooth == "Acceleration" && generalApply()
    }

    open val modernDynamicAccelCoefValue = float("CoefDistance", -1.393f, -2f..2f) {
        modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernYawCrosshairAccelValue = floatRange("YawCrosshairAccel", 17f..20f, 1f..180f) {
        modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernPitchCrosshairAccelValue = floatRange("PitchCrosshairAccel", 17f..20f, 1f..180f) {
        modernAngleSmooth == "Acceleration" && modernDynamicAccel && generalApply()
    }

    open val modernShortStopValue = boolean("ShortStop", false) {
        rotationsActive && generalApply()
    }.apply { aliases("SimulateShortStop") }

    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) {
        modernShortStop && generalApply()
    }

    open val modernShortStopRateValue = int("ShortStopRate", 3, 1..25, "%") {
        modernShortStop && generalApply()
    }

    open val modernFailValue = boolean("Fail", false) {
        rotationsActive && generalApply()
    }

    open val modernFailRateValue = int("FailRate", 3, 1..100, "%") {
        modernFail && generalApply()
    }

    open val modernFailFactorValue = float("FailFactor", 0.04f, 0.01f..0.99f) {
        modernFail && generalApply()
    }

    open val modernFailStrengthHorizontalValue = floatRange("FailStrengthHorizontal", 5f..10f, 1f..90f, "deg") {
        modernFail && generalApply()
    }

    open val modernFailStrengthVerticalValue = floatRange("FailStrengthVertical", 0f..2f, 0f..90f, "deg") {
        modernFail && generalApply()
    }

    open val modernFailTransitionDurationValue = intRange("FailTransitionInDuration", 1..4, 0..20, "ticks") {
        modernFail && generalApply()
    }

    // Variables for easier access
    val rotations by rotationsValue
    val applyServerSide by applyServerSideValue
    val shortStopDuration by shortStopDurationValue
    val keepRotation by keepRotationValue
    val maximumRotationDifference by maximumRotationDifferenceValue
    val modernAngleSmooth by modernAngleSmoothValue
    val modernMovementCorrection by modernMovementCorrectionValue
    val modernResetThreshold by modernResetThresholdValue
    val modernTicksUntilReset by modernTicksUntilResetValue
    val ignoreOpenInventory by ignoreOpenInventoryValue
    val modernHorizontalTurnSpeed by modernHorizontalTurnSpeedValue
    val modernVerticalTurnSpeed by modernVerticalTurnSpeedValue
    val modernSigmoidSteepness by modernSigmoidSteepnessValue
    val modernSigmoidMidpoint by modernSigmoidMidpointValue
    val modernInterpolationHorizontalSpeed by modernInterpolationHorizontalSpeedValue
    val modernInterpolationVerticalSpeed by modernInterpolationVerticalSpeedValue
    val modernInterpolationDirectionChangeFactor by modernInterpolationDirectionChangeFactorValue
    val modernInterpolationMidpoint by modernInterpolationMidpointValue
    val modernAiYawMultiplier by modernAiYawMultiplierValue
    val modernAiPitchMultiplier by modernAiPitchMultiplierValue
    val modernAiCorrection by modernAiCorrectionValue
    val modernAiCorrectionHorizontalSpeed by modernAiCorrectionHorizontalSpeedValue
    val modernAiCorrectionVerticalSpeed by modernAiCorrectionVerticalSpeedValue
    val modernAiCorrectionDirectionChange by modernAiCorrectionDirectionChangeValue
    val modernAiCorrectionLinearHorizontal by modernAiCorrectionLinearHorizontalValue
    val modernAiCorrectionLinearVertical by modernAiCorrectionLinearVerticalValue
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

    open val rotationsActive
        get() = rotations

    val effectiveResetTicks
        get() = modernTicksUntilReset

    /** Source-compatible aliases for modules not yet renamed to the Nextgen terminology. */
    val resetTicksValue
        get() = modernTicksUntilResetValue
    val resetTicks
        get() = modernTicksUntilReset

    val effectiveRequestPriority
        get() = if (prioritizeRequest) RotationPriority.CRITICAL.level else requestPriority.level

    fun withoutKeepRotation() = apply {
        keepRotationValue.excludeWithState()
    }

    fun withRequestPriority(priority: RotationPriority) = apply {
        requestPriority = priority
    }

    /** Restores the flat settings bundle as a nested configurable. */
    fun nestInto(parent: Configurable) = apply {
        // Excluded values are implementation switches and must stay out of the public tree.
        // Re-adding them here can create duplicate keys (for example Scaffold's rotation mode).
        addValues(flattenedValues.filterNot { it.excluded })
        parent.addValue(this)
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
