package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue

class VerusBasicFly : FlyMode("VerusBasic") {
    
    override fun onMove(event: MoveEvent) {
        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            event.y = 0.42
        }else {
            mc.thePlayer.motionY = 0.0;
            event.y = 0.0
            MovementUtils.strafe(0.35f)
        }
    }
}
