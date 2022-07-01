package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class HYT4V4 : LongJumpMode("HYT4V4") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.031470000997
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.0114514f)
        mc.timer.timerSpeed = 1.0114514f
    }
}