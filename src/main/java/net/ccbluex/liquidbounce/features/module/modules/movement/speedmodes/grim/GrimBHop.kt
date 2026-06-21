/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.grim

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Timer-free bunny hop tuned for Grim-style movement checks, which punish game-speed
 * manipulation. Jumps and re-applies strafe on the ground tick only, keeping the air phase
 * vanilla so prediction stays consistent.
 */
object GrimBHop : SpeedMode("GrimBHop") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder || !player.isMoving) {
            return
        }

        if (player.onGround) {
            player.tryJump()
            strafe(0.36f)
        }
    }
}
