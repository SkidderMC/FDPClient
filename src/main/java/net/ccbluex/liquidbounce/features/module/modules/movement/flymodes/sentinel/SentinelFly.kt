/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.sentinel

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Sentinel glide that opens up while you are taking knockback: it cruises with jump/sneak
 * vertical control and briefly raises the horizontal push during the hurt window, when the
 * server already tolerates higher velocity.
 */
object SentinelFly : FlyMode("SentinelFly") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.motionY = when {
            mc.gameSettings.keyBindJump.isKeyDown -> 0.2
            mc.gameSettings.keyBindSneak.isKeyDown -> -0.2
            else -> 0.0
        }

        strafe(if (player.hurtTime > 0) 0.4f else 0.3f)
    }
}
