/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving

object MatrixSlowHop : SpeedMode("MatrixSlowHop") {

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.isMoving) {
            if (!player.onGround && player.fallDistance > 2) {
                mc.timer.timerSpeed = 1f
                return
            }

            if (player.onGround) {
                player.motionY = 0.42 - if (Speed.matrixLowHop) 3.48E-3 else 0.0
                mc.timer.timerSpeed = 0.5195f
                strafe(speed + Speed.extraGroundBoost)
            } else {
                mc.timer.timerSpeed = 1.0973f
            }

            if (player.fallDistance <= 0.4 && player.moveStrafing == 0f) {
                player.speedInAir = 0.02035f
            } else {
                player.speedInAir = 0.02f
            }
        } else {
            mc.timer.timerSpeed = 1f
        }
    }
}