/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.polar

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

/**
 * Steady cruise glide for Polar: constant horizontal strafe with jump/sneak vertical control
 * and a slow passive sink when neither is held, so the descent reads like ordinary falling.
 */
object PolarFly : FlyMode("PolarFly") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.motionY = when {
            mc.gameSettings.keyBindJump.isKeyDown -> 0.22
            mc.gameSettings.keyBindSneak.isKeyDown -> -0.22
            else -> -0.04
        }

        strafe(0.3f)
    }
}
