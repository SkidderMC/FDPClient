/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class IntaveHop : SpeedMode("IntaveHop") {
  
    private var jumpTicks = 0
  
    override fun onPreMotion() {
        if (mc.thePlayer.onGround) {
            MovementUtils.strafe(0.588)
            mc.thePlayer.jump()
            jumpTicks = 0
        } else if (jumpTicks == 4) {
            mc.thePlayer.motionY = -0.0784000015258789
            jumpTicks++
        } else {
            jumpTicks++
        }

        MovementUtils.strafe()

    }
}
