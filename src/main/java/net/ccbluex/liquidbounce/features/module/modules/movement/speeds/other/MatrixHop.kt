package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixHop : SpeedMode("MatrixHop") {
    private var ticks=0

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                MovementUtils.strafe()
                ticks++
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }


        if(mc.thePlayer.onGround){
            ticks++
        }
        mc.thePlayer.onGround=ticks>2
        if(mc.thePlayer.onGround) {
            ticks = 0
        }
    }
}