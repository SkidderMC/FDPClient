/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump

object HypixelHop : SpeedMode("HypixelHop") {
    override fun onStrafe() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid)
            return

        if (player.onGround && player.isMoving) {
            if (player.isUsingItem) {
                player.tryJump()
            } else {
                player.tryJump()
                strafe(0.48f)
            }
        }

    }
}
