/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AACLowHop2 : SpeedMode("AACLowHop2") {
    private var legitJump = false

    override fun onEnable() {
        legitJump = true
        mc.timer.timerSpeed = 1f
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPreMotion() {
        mc.timer.timerSpeed = 1f

        if (mc.thePlayer.isInWater) return

        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.09f
            if (mc.thePlayer.onGround) {
                if (legitJump) {
                    mc.thePlayer.jump()
                    legitJump = false
                    return
                }
                mc.thePlayer.motionY = 0.343
                MovementUtils.strafe(0.534f)
            }
        } else {
            legitJump = true
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}