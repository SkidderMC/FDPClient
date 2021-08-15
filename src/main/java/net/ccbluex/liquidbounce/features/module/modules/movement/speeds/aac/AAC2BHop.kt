/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AAC2BHop : SpeedMode("AAC2BHop") {
    override fun onPreMotion() {
        if (mc.thePlayer.isInWater) return

        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionX *= 1.02
                mc.thePlayer.motionZ *= 1.02
            } else if (mc.thePlayer.motionY > -0.2) {
                mc.thePlayer.jumpMovementFactor = 0.08f
                mc.thePlayer.motionY += 0.0143099999999999999999999999999
                mc.thePlayer.jumpMovementFactor = 0.07f
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}