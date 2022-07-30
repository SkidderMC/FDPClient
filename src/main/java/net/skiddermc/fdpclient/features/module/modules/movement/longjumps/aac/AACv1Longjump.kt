package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.aac

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class AACv1Longjump : LongJumpMode("AACv1") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.05999
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.08f)
    }
}