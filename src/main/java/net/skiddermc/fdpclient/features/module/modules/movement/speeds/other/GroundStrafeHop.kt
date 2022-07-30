package net.skiddermc.fdpclient.features.module.modules.movement.speeds.other

import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils

class GroundStrafeHop : SpeedMode("GroundStrafeHop") {
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
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