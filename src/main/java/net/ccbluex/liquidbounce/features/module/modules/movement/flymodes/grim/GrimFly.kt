/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.grim

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Low-amplitude glide for Grim: holds a steady horizontal strafe while jump/sneak nudge you
 * up or down by a small amount, keeping vertical motion gentle to avoid sharp prediction
 * deltas. Best-effort against modern Grim builds — tune the values to the server.
 */
object GrimFly : FlyMode("GrimFly") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.motionY = when {
            mc.gameSettings.keyBindJump.isKeyDown -> 0.18
            mc.gameSettings.keyBindSneak.isKeyDown -> -0.18
            else -> 0.0
        }

        strafe(0.28f)
    }
}
