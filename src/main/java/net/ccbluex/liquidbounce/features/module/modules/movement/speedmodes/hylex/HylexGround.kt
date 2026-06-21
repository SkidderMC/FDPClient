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
 * Stays on the ground and keeps re-applying strafe each tick for a glide-style boost,
 * for Hylex setups that watch jump cadence more closely than flat ground speed.
 */
object HylexGround : SpeedMode("HylexGround") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder || !player.isMoving) {
            return
        }

        if (player.onGround) {
            strafe(0.28f)
        }
    }
}
