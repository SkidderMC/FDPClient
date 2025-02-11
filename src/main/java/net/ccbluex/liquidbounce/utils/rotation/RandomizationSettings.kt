/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.plus
import net.ccbluex.liquidbounce.utils.extensions.random
import net.ccbluex.liquidbounce.utils.extensions.times
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.angleDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.lastRotations
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import kotlin.math.sign

class RandomizationSettings(owner: Module, val generalApply: () -> Boolean = { true }) : Configurable("Randomization") {

    private val randomizationPattern by choices(
        "RandomizationPattern", arrayOf("None", "Zig-Zag", "LazyFlick"), "None"
    ) { generalApply() }
    private val yawRandomizationChance by floatRange(
        "YawRandomizationChance", 0.8f..1.0f, 0f..1f
    ) { randomizationChosen }
    private val yawRandomizationRange by floatRange(
        "YawRandomizationRange",
        5f..10f,
        0f..30f
    ) { isZizZagActive && randomizationChosen && yawRandomizationChance.start != 1F }
    private val yawSpeedIncreaseMultiplier by intRange(
        "YawSpeedIncreaseMultiplier", 50..120, 0..500, suffix = "%"
    ) { !isZizZagActive && randomizationChosen && yawRandomizationChance.start != 1F }
    private val pitchRandomizationChance by floatRange(
        "PitchRandomizationChance", 0.8f..1.0f, 0f..1f
    ) { randomizationChosen }
    private val pitchRandomizationRange by floatRange(
        "PitchRandomizationRange",
        5f..10f,
        0f..30f
    ) { randomizationChosen && pitchRandomizationChance.start != 1F }

    private val isZizZagActive
        get() = randomizationPattern == "Zig-Zag"

    val randomizationChosen
        get() = randomizationPattern != "None" && generalApply()

    fun processNextSpot(box: AxisAlignedBB, rotation: Rotation, eyes: Vec3, range: Double) {
        val intercept = box.calculateIntercept(eyes, eyes + getVectorForRotation(lastRotations.random()) * range)

        // Smooth out randomized rotation pattern using previous rotation to simulate natural movement
        val pitchMovement =
            angleDifference(rotation.pitch, lastRotations[2].pitch).sign.takeIf { it != 0f } ?: (-1..1).random()
                .toFloat()
        val yawMovement = angleDifference(rotation.yaw, lastRotations[2].yaw)

        val yawSign = yawMovement.sign.takeIf { it != 0f } ?: arrayOf(-1f, 1f).random()

        val yawIncrease = if (Math.random() > yawRandomizationChance.random()) {
            if (!isZizZagActive) {
                yawSpeedIncreaseMultiplier.random() / 100f * yawMovement
            } else {
                yawRandomizationRange.random() * yawSign
            }
        } else 0f

        val pitchIncrease = if (Math.random() > pitchRandomizationChance.random()) {
            if (!isZizZagActive) {
                pitchRandomizationRange.random() + pitchMovement
            } else {
                pitchRandomizationRange.random() * pitchMovement
            }
        } else 0f

        if (isZizZagActive || intercept?.hitVec == null) {
            rotation.yaw += yawIncrease
            rotation.pitch += pitchIncrease

            rotation.fixedSensitivity()
        }
    }

    init {
        owner.addValues(this.values)
    }
}