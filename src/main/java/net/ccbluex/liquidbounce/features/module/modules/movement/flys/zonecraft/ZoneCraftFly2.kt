package net.ccbluex.liquidbounce.features.module.modules.movement.flys.zonecraft

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class ZoneCraftFly2 : FlyMode("ZoneCraft2") {
    
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f
        if(mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42
        } else {
            mc.thePlayer.motionY = 0.0
            MovementUtils.strafe(0.259f)
            
            mc.thePlayer.posY -= (mc.thePlayer.posY % 0.1152)

        }
    }
}
