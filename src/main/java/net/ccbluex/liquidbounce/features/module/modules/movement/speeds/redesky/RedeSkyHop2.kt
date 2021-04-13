package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class RedeSkyHop2 : SpeedMode("RedeSkyHop2") {
    private var delay=0;

    override fun onMotion() {
        if(MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                delay = 0
            } else {
                if (delay in 3..4) {
                    mc.thePlayer.jumpMovementFactor = 0.1F
                }
                delay++
            }
        }
    }
}