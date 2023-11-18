/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue

class MinemenHopSpeed : SpeedMode("MinemenHop") {
    
    private val veloAbuseValue = BoolValue("KnockbackAbuse", false)
    
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.hurtTime < 6 || veloAbuseValue.get()) {
                MovementUtils.strafe()
            }
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                MovementUtils.strafe()
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
