/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.grim

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Web escape tuned for newer Grim builds: keeps a gentle, consistent jump-and-strafe cadence
 * on the ground so the web slowdown is shed without the sharp velocity spikes Grim flags.
 */
object NewGrim : NoWebMode("NewGrim") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (!player.isInWeb || !player.isMoving) {
            return
        }

        if (player.onGround) {
            player.tryJump()
            strafe(0.28f)
        }
    }
}
