package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class ZoneCraftFly : FlyMode("ZoneCraft") {
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f
        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            event.y = 0.42
        }else {
            mc.thePlayer.motionY = 0.0;
            event.y = 0.0
            if(mc.thePlayer.ticksExisted % 20 < 10) {
                mc.timer.timerSpeed = 1.25f
            } else {
                mc.timer.timerSpeed = 0.8f
            }
            if(mc.thePlayer.ticksExisted % 20 == 9) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.125f)
            }
            if(mc.thePlayer.ticksExisted % 20 == 1) {
                MovementUtils.strafe((0.2783*1.2).toFloat())
            }
        }
    }
}