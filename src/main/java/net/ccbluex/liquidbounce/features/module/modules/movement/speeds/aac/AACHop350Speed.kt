/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AACHop350Speed : SpeedMode("AACHop3.5.0") {
    override fun onMotion(event: MotionEvent) {
        if (event.eventState === EventState.POST && MovementUtils.isMoving() && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava) {
            mc.thePlayer.jumpMovementFactor += 0.00208f
            if (mc.thePlayer.fallDistance <= 1f) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionX *= 1.0118
                    mc.thePlayer.motionZ *= 1.0118
                } else {
                    mc.thePlayer.motionY -= 0.0147
                    mc.thePlayer.motionX *= 1.00138
                    mc.thePlayer.motionZ *= 1.00138
                }
            }
        }
    }

    override fun onEnable() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
    }

    override fun onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
    }
}