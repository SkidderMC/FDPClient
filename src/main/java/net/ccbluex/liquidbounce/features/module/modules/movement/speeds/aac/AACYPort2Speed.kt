/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AACYPort2Speed : SpeedMode("AACYPort2") {
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.thePlayer.cameraPitch = 0f
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.3851
                mc.thePlayer.motionX *= 1.01
                mc.thePlayer.motionZ *= 1.01
            } else mc.thePlayer.motionY = -0.21
        }
    }
}