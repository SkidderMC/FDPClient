/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.sentinel

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Sentinel speed that leans on the brief leniency window right after taking knockback:
 * it bunny hops normally but pushes harder while hurt, when the server expects elevated
 * velocity anyway.
 */
object SentinelSpeed : SpeedMode("SentinelSpeed") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInLiquid || player.isInWeb || player.isOnLadder || !player.isMoving) {
            return
        }

        if (player.onGround) {
            player.tryJump()
            strafe(if (player.hurtTime > 0) 0.45f else 0.36f)
        }
    }
}
