/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.features

import net.ccbluex.liquidbounce.config.ToggleableValueGroup
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.rotation.point.PointInsideBox
import net.minecraft.util.Vec3
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.math.hypot

class PointProcessorGaussian : PointProcessor("Gaussian", false), MinecraftInstance {

    private companion object {
        const val STDDEV_Z = 0.24453708645460387
        const val MEAN_X = 0.00942273861037109
        const val STDDEV_X = 0.23319837528201348
        const val MEAN_Y = -0.30075078007595923
        const val STDDEV_Y = 0.3492437109081718
        const val MEAN_Z = 0.013282929419023442

        val random = SecureRandom()
    }

    private var currentOffset: Vec3 = Vec3(0.0, 0.0, 0.0)
    private var targetOffset: Vec3 = Vec3(0.0, 0.0, 0.0)

    private val yawFactor = gatedFloatRange("YawOffset", 0f..0f, 0.0f..1.0f)
    private val pitchFactor = gatedFloatRange("PitchOffset", 0f..0f, 0.0f..1.0f)
    private val chance by gatedInt("Chance", 100, 0..100, "%")
    private val speed = gatedFloatRange("Speed", 0.1f..0.2f, 0.01f..1f)
    private val tolerance by gatedFloat("Tolerance", 0.05f, 0.01f..0.1f)

    private inner class Dynamic : ToggleableValueGroup("Dynamic", false) {
        val hurtTime by gatedInt("HurtTime", 10, 0..10)
        val yawFactor by gatedFloat("YawFactor", 0f, 0f..10f, "x")
        val pitchFactor by gatedFloat("PitchFactor", 0f, 0f..10f, "x")
        val speed = gatedFloatRange("Speed", 0.5f..0.75f, 0.01f..1f)
        val tolerance by gatedFloat("Tolerance", 0.1f, 0.01f..0.1f)
    }

    private val dynamic = Dynamic()

    init {
        +dynamic
    }

    private fun rollSpeed(dynamicCheck: Boolean): Double {
        val range = if (dynamicCheck) dynamic.speed.get() else speed.get()
        return nextFloat(range.start, range.endInclusive).toDouble()
    }

    private fun gaussian(mean: Double, stddev: Double): Double = random.nextGaussian() * stddev + mean

    private fun horizontalSpeed(): Double {
        val player = mc.thePlayer ?: return 0.0
        return hypot(player.motionX, player.motionZ)
    }

    private fun updateGaussianOffset() {
        val player = mc.thePlayer
        val dynamicCheck = dynamic.enabled && player != null && player.hurtTime >= dynamic.hurtTime

        val rolledYaw = nextFloat(yawFactor.get().start, yawFactor.get().endInclusive)
        val rolledPitch = nextFloat(pitchFactor.get().start, pitchFactor.get().endInclusive)

        val yaw = if (dynamicCheck && dynamic.yawFactor > 0f) {
            rolledYaw + horizontalSpeed() * dynamic.yawFactor
        } else {
            rolledYaw.toDouble()
        }

        val pitch = if (dynamicCheck && dynamic.pitchFactor > 0f) {
            rolledPitch + horizontalSpeed() * dynamic.pitchFactor
        } else {
            rolledPitch.toDouble()
        }

        val effectiveTolerance = (if (dynamicCheck) dynamic.tolerance else tolerance).toDouble()
        val withinTolerance = abs(currentOffset.xCoord - targetOffset.xCoord) <= effectiveTolerance &&
            abs(currentOffset.yCoord - targetOffset.yCoord) <= effectiveTolerance &&
            abs(currentOffset.zCoord - targetOffset.zCoord) <= effectiveTolerance

        if (withinTolerance) {
            if (random.nextInt(100) <= chance) {
                targetOffset = Vec3(
                    gaussian(MEAN_X, STDDEV_X) * yaw,
                    gaussian(MEAN_Y, STDDEV_Y) * pitch,
                    gaussian(MEAN_Z, STDDEV_Z) * yaw
                )
            }
        } else {
            currentOffset = Vec3(
                lerp(rollSpeed(dynamicCheck), currentOffset.xCoord, targetOffset.xCoord),
                lerp(rollSpeed(dynamicCheck), currentOffset.yCoord, targetOffset.yCoord),
                lerp(rollSpeed(dynamicCheck), currentOffset.zCoord, targetOffset.zCoord)
            )
        }
    }

    private fun lerp(delta: Double, start: Double, end: Double): Double = start + (end - start) * delta

    override fun process(point: PointInsideBox): PointInsideBox {
        val maxYaw = yawFactor.get().endInclusive
        val maxPitch = pitchFactor.get().endInclusive
        if (maxYaw > 0.0f && maxPitch > 0.0f && chance > 0) {
            updateGaussianOffset()
        }

        return point + currentOffset
    }
}
