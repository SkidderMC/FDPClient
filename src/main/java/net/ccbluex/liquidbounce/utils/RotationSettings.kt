/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.withGCD
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.int
import net.ccbluex.liquidbounce.value.intRange

@Suppress("MemberVisibilityCanBePrivate")
open class RotationSettings(owner: Module, generalApply: () -> Boolean = { true }) {

    open val rotationsValue = boolean("Rotations", true) { generalApply() }

    open val smootherModeValue = choices(
        "SmootherMode", arrayOf("Linear", "Relative"), "Relative"
    ) { rotationsActive && generalApply() }
    open val applyServerSideValue = boolean("ApplyServerSide", true) { rotationsActive && generalApply() }
    open val simulateShortStopValue = boolean("SimulateShortStop", false) { rotationsActive && generalApply() }
    open val shortStopChanceValue = int("ShortStopChance", 3, 1..25) { simulateShortStop }
    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) { simulateShortStop }
    open val strafeValue = boolean("Strafe", false) { rotationsActive && applyServerSide && generalApply() }
    open val strictValue = boolean("Strict", false) { strafeValue.isActive() && generalApply() }
    open val keepRotationValue = boolean(
        "KeepRotation", true
    ) { rotationsActive && applyServerSide && generalApply() }
    open val resetTicksValue = object : IntegerValue("ResetTicks", 1, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minimum)
        override fun isSupported() = rotationsActive && applyServerSide && generalApply()
    }

    open val legitimizeValue = boolean("Legitimize", false) { rotationsActive && generalApply() }
    open val maxHorizontalAngleChangeValue: FloatValue = object : FloatValue(
        "MaxHorizontalAngleChange", 180f, 1f..180f
    ) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalAngleChange)
        override fun isSupported() = rotationsActive && generalApply()
    }

    open val minHorizontalAngleChangeValue: FloatValue = object : FloatValue(
        "MinHorizontalAngleChange", 180f, 1f..180f
    ) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalAngleChange)
        override fun isSupported() = !maxHorizontalAngleChangeValue.isMinimal() && rotationsActive && generalApply()
    }

    open val maxVerticalAngleChangeValue: FloatValue = object : FloatValue("MaxVerticalAngleChange", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minVerticalAngleChange)
        override fun isSupported() = rotationsActive && generalApply()
    }

    open val minVerticalAngleChangeValue: FloatValue = object : FloatValue("MinVerticalAngleChange", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxVerticalAngleChange)
        override fun isSupported() = !maxVerticalAngleChangeValue.isMinimal() && rotationsActive && generalApply()
    }

    open val angleResetDifferenceValue: FloatValue = object : FloatValue("AngleResetDifference", 5f.withGCD(), 0.0f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.withGCD().coerceIn(range)
        override fun isSupported() = rotationsActive && applyServerSide && generalApply()
    }

    open val minRotationDifferenceValue = FloatValue(
        "MinRotationDifference", 0f, 0f..1f
    ) { rotationsActive && generalApply() }

    // Variables for easier access
    val rotations by rotationsValue
    val smootherMode by smootherModeValue
    val applyServerSide by applyServerSideValue
    val simulateShortStop by simulateShortStopValue
    val shortStopChance by shortStopChanceValue
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

    init {
        owner.addConfigurable(this)
    }
}

class RotationSettingsWithRotationModes(
    owner: Module, listValue: ListValue, generalApply: () -> Boolean = { true },
) : RotationSettings(owner, generalApply) {

    override val rotationsValue = super.rotationsValue.apply { excludeWithState() }

    val rotationModeValue = listValue.apply { isSupported = generalApply }

    val rotationMode by rotationModeValue

    override val rotationsActive: Boolean
        get() = rotationMode != "Off"

    init {
        owner.addConfigurable(this)
    }
}