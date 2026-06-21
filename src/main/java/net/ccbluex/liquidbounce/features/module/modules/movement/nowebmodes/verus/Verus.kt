/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Verus web escape: a firm ground hop with a strong launch strafe followed by a lighter air
 * strafe, leaning on Verus's leniency for jump-initiated horizontal speed.
 */
object Verus : NoWebMode("Verus") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (!player.isInWeb || !player.isMoving) {
            return
        }

        if (player.onGround) {
            player.tryJump()
            strafe(0.42f)
        } else {
            strafe(0.3f)
        }
    }
}
