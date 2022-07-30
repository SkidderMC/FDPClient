package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.blocksmc

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class OldBlocksMC2Longjump : LongJumpMode("OldBlocksMC2") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.01554
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.114514f)
        mc.timer.timerSpeed = 0.917555f
    }
}