/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving

object MatrixHop : SpeedMode("MatrixHop") {

    override fun onUpdate()  {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.isMoving) {
            if (!player.onGround && player.fallDistance > 1.215f) {
                mc.timer.timerSpeed = 1f
                return
            }

            if (player.onGround) {
                strafe(speed + Speed.extraGroundBoost)
                player.motionY = 0.42 - if (Speed.matrixLowHop) 3E-3 else 0.0
                if (player.motionY > 0) {
                    if (Speed.timerSpeed) mc.timer.timerSpeed = 1.0853f
                }
            } else {
                if (player.motionY < 0) {
                    if (Speed.timerSpeed) mc.timer.timerSpeed = 0.9185f
                }
            }
        } else {
            if (Speed.timerSpeed) mc.timer.timerSpeed = 1f
        }
    }
}