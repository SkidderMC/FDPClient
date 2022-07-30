/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement.speeds.other

import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils

class IntaveHop : SpeedMode("IntaveHop") {
  
    private var jumpTicks = 0
  
    override fun onPreMotion() {
        jumpTicks++
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            jumpTicks = 0
            return
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            MovementUtils.strafe(0.588f)
            mc.thePlayer.jump()
            jumpTicks = 0
        } else if (jumpTicks == 5) {
            mc.thePlayer.motionY = -0.0784000015258789
        }
        MovementUtils.strafe()
    }
}
