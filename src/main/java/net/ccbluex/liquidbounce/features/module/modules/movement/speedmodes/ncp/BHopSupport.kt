package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin

internal fun MinecraftInstance.updateBHopTimer(timerDelay: Int): Int {
    val nextDelay = (timerDelay + 1) % 5

    if (nextDelay != 0) {
        mc.timer.timerSpeed = 1f
        return nextDelay
    }

    if (mc.thePlayer.isMoving) {
        mc.timer.timerSpeed = 32767f
        mc.timer.timerSpeed = 1.3f
        mc.thePlayer.motionX *= 1.0199999809265137
        mc.thePlayer.motionZ *= 1.0199999809265137
    }

    return 0
}

internal fun roundedBHopValue(value: Double): Double =
    BigDecimal(value).setScale(3, RoundingMode.HALF_UP).toDouble()

internal fun applyBHopDirection(
    event: MoveEvent,
    moveSpeed: Double,
    movementForward: Float,
    movementStrafe: Float,
    yaw: Float,
) {
    var forward = movementForward
    var strafe = movementStrafe
    var rotationYaw = yaw

    if (forward == 0f && strafe == 0f) {
        event.zeroXZ()
        return
    }

    if (forward != 0f) {
        if (strafe >= 1f) {
            rotationYaw += if (forward > 0f) -45f else 45f
            strafe = 0f
        } else if (strafe <= -1f) {
            rotationYaw += if (forward > 0f) 45f else -45f
            strafe = 0f
        }

        forward = when {
            forward > 0f -> 1f
            forward < 0f -> -1f
            else -> forward
        }
    }

    val radians = Math.toRadians((rotationYaw + 90f).toDouble())
    val cosine = cos(radians)
    val sine = sin(radians)

    event.x = forward * moveSpeed * cosine + strafe * moveSpeed * sine
    event.z = forward * moveSpeed * sine - strafe * moveSpeed * cosine
}
