package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.other

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class HYT4V4Longjump : LongJumpMode("HYT4V4") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.031470000997
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.0114514f)
        mc.timer.timerSpeed = 1.0114514f
    }
}