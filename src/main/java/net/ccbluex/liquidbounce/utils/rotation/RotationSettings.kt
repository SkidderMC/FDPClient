/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.withGCD
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
open class RotationSettings(owner: Module, generalApply: () -> Boolean = { true }) : Configurable("RotationSettings") {

    open val rotationsValue = boolean("Rotations", true) { generalApply() }
    open val applyServerSideValue = boolean("ApplyServerSide", true) { rotationsActive && generalApply() }
    open val simulateShortStopValue = boolean("SimulateShortStop", false) { rotationsActive && generalApply() }
    open val rotationDiffBuildUpToStopValue = float("RotationDiffBuildUpToStop", 180f, 50f..720f) { simulateShortStop }
    open val maxThresholdAttemptsToStopValue = int("MaxThresholdAttemptsToStop", 1, 0..5) { simulateShortStop }
    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) { simulateShortStop }
    open val strafeValue = boolean("Strafe", false) { rotationsActive && applyServerSide && generalApply() }
    open val strictValue = boolean("Strict", false) { strafeValue.isActive() && generalApply() }
    open val keepRotationValue = boolean("KeepRotation", true) { rotationsActive && applyServerSide && generalApply() }

    open val resetTicksValue: Value<Int> = int("ResetTicks", 1, 1..20) {
        rotationsActive && applyServerSide && keepRotation && generalApply()
    }.onChange { _, new ->
        new.coerceAtLeast(1) // minimum
    }

    open val legitimizeValue = boolean("Legitimize", false) { rotationsActive && generalApply() }
    open val maxHorizontalAngleChangeValue = float("MaxHorizontalAngleChange", 180f, 1f..180f) {
        rotationsActive && generalApply()
    }.onChange { _, new ->
        new.coerceAtLeast(minHorizontalAngleChange)
    }
    open val minHorizontalAngleChangeValue: Value<Float> = float("MinHorizontalAngleChange", 180f, 1f..180f) {
        rotationsActive && generalApply()
    }.onChange { _, new ->
        new.coerceAtMost(maxHorizontalAngleChange)
    }

    open val maxVerticalAngleChangeValue: Value<Float> = float("MaxVerticalAngleChange", 180f, 1f..180f) {
        rotationsActive && generalApply()
    }.onChange { _, new ->
        new.coerceAtLeast(minVerticalAngleChange)
    }

    open val minVerticalAngleChangeValue: Value<Float> = float("MinVerticalAngleChange", 180f, 1f..180f) {
        rotationsActive && generalApply()
    }.onChange { _, new ->
        new.coerceAtMost(maxVerticalAngleChange)
    }

    open val angleResetDifferenceValue = float("AngleResetDifference", 5f.withGCD(), 0.0f..180f) {
        rotationsActive && applyServerSide && generalApply()
    }.onChange { _, new ->
        new.withGCD().coerceIn(0.0f..180f) // range
    }

    open val minRotationDifferenceValue = float(
        "MinRotationDifference", 2f, 0f..4f
    ) { rotationsActive && generalApply() }

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
    val maxHorizontalAngleChange by maxHorizontalAngleChangeValue
    val minHorizontalAngleChange by minHorizontalAngleChangeValue
    val maxVerticalAngleChange by maxVerticalAngleChangeValue
    val minVerticalAngleChange by minVerticalAngleChangeValue
    val angleResetDifference by angleResetDifferenceValue
    val minRotationDifference by minRotationDifferenceValue

    var prioritizeRequest = false
    var immediate = false
    var instant = false

    var rotDiffBuildUp = 0f
    var maxThresholdReachAttempts = 0

    open val rotationsActive
        get() = rotations

    val horizontalSpeed
        get() = minHorizontalAngleChange..maxHorizontalAngleChange

    val verticalSpeed
        get() = minVerticalAngleChange..maxVerticalAngleChange

    fun withoutKeepRotation(): RotationSettings {
        keepRotationValue.excludeWithState()

        return this
    }

    fun updateSimulateShortStopData(diff: Float) {
        rotDiffBuildUp += diff
    }

    fun resetSimulateShortStopData() {
        rotDiffBuildUp = 0f
        maxThresholdReachAttempts = 0
    }

    fun shouldPerformShortStop(): Boolean {
        if (abs(rotDiffBuildUp) < rotationDiffBuildUpToStop || !simulateShortStop)
            return false

        if (maxThresholdReachAttempts < maxThresholdAttemptsToStop) {
            maxThresholdReachAttempts++
            return false
        }

        return true
    }

    init {
        owner.addValues(this.values)
    }
}

class RotationSettingsWithRotationModes(
    owner: Module, listValue: ListValue, generalApply: () -> Boolean = { true },
) : RotationSettings(owner, generalApply) {

    override val rotationsValue = super.rotationsValue.apply { excludeWithState() }

    val rotationModeValue = listValue.setSupport { generalApply() }

    val rotationMode by rotationModeValue

    override val rotationsActive: Boolean
        get() = rotationMode != "Off"

    init {
        owner.addValues(this.values)
    }
}