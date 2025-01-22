/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving

object MatrixHop : SpeedMode("MatrixHop") {

    override fun onUpdate()  {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder) return

        if (Speed.matrixLowHop) player.jumpMovementFactor = 0.026f

        if (player.isMoving) {
            if (player.onGround) {
                strafe(if (!Scaffold.handleEvents()) speed + Speed.extraGroundBoost else speed)
                player.motionY = 0.42 - if (Speed.matrixLowHop) 3.48E-3 else 0.0
            } else {
                if (!Scaffold.handleEvents() && speed < 0.19) {
                    strafe()
                }
            }

            if (player.fallDistance <= 0.4 && player.moveStrafing == 0f) {
                player.speedInAir = 0.02035f
            } else {
                player.speedInAir = 0.02f
            }
        }
    }
}