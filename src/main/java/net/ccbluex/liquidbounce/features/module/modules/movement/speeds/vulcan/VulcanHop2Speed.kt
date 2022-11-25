package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanHop2Speed : SpeedMode("VulcanHop2") {

    private var jumpTicks = 0

    override fun onUpdate() {

        jumpTicks += 1

        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()

                jumpTicks = 0
            } else {
                if (jumpTicks > 3)
                    mc.thePlayer.motionY = (mc.thePlayer.motionY - 0.08) * 0.98

                MovementUtils.strafe(MovementUtils.getSpeed() * (1.01 - (Math.random() / 500)).toFloat() )
            }
        } else {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
