package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.blocksmc

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class OldBlocksMC2 : LongJumpMode("OldBlocksMC2") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.01554
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.114514f)
        mc.timer.timerSpeed = 0.917555f
    }
}