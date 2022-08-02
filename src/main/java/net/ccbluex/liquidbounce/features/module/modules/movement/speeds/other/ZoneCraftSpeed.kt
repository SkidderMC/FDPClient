package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class ZoneCraftSpeed : SpeedMode("ZoneCraft") {
    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                MovementUtils.strafe(0.89f)
                mc.thePlayer.motionY = 0.38
                event.y = 0.38
            } else {
                event.x = 0.0
                event.z = 0.0
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }
        }
    }
}