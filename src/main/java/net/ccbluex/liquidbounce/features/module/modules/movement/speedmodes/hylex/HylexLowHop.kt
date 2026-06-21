/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hylex

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Short, repeated hops for Hylex: a small vertical pop off the ground paired with a moderate
 * strafe, keeping average height low to avoid obvious jump-pattern flags.
 */
object HylexLowHop : SpeedMode("HylexLowHop") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder || !player.isMoving) {
            return
        }

        if (player.onGround) {
            player.motionY = 0.36
            strafe(0.34f)
        }
    }
}
