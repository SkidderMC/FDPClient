/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Vulcan web escape: restores air control with a raised jump movement factor and pulses a
 * stronger strafe every other tick to climb out of the web while staying within Vulcan's
 * movement tolerance.
 */
object Vulcan : NoWebMode("Vulcan") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (!player.isInWeb || !player.isMoving) {
            return
        }

        player.jumpMovementFactor = 0.05f

        if (player.onGround) {
            player.tryJump()
        }

        if (player.ticksExisted % 2 == 0) {
            strafe(0.4f)
        }
    }
}
