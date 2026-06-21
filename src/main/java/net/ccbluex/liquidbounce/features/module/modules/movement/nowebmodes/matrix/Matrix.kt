/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Matrix web escape: alternates a high ground strafe with a jump-and-strafe tick to break the
 * web's grip quickly while keeping the motion pattern irregular enough to slip Matrix checks.
 */
object Matrix : NoWebMode("Matrix") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (!player.isInWeb || !player.isMoving || !player.onGround) {
            return
        }

        if (player.ticksExisted % 2 == 0) {
            strafe(0.6f)
        } else {
            player.tryJump()
            strafe(0.3f)
        }
    }
}
