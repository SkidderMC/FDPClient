/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement.speeds.other

import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils

class Legit : SpeedMode("Legit") {
    override fun onPreMotion() {
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) mc.thePlayer.jump()
        }
    }
}
