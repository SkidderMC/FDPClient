package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.blocksmc

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class OldBlocksMCLongjump : LongJumpMode("OldBlocksMC") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.jumpMovementFactor = 0.1f
        mc.thePlayer.motionY += 0.0132
        mc.thePlayer.jumpMovementFactor = 0.09f
        mc.timer.timerSpeed = 0.8f
        MovementUtils.strafe()
    }
}