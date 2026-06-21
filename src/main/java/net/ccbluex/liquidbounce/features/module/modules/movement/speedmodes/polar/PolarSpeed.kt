/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.polar

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Conservative hop for Polar: a gentle ground timer to stretch the launch tick, eased back
 * to normal on descent so the overall tick balance stays close to vanilla.
 */
object PolarSpeed : SpeedMode("PolarSpeed") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder || !player.isMoving) {
            mc.timer.timerSpeed = 1f
            return
        }

        if (player.onGround) {
            player.tryJump()
            strafe(0.33f)
            mc.timer.timerSpeed = 1.05f
        } else if (player.motionY < 0) {
            mc.timer.timerSpeed = 1f
        }
    }
}
