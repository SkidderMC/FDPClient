package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class RedeSkyTestLongjump : LongJumpMode("RedeSkyTest") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY = 0.42
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.12f)
        mc.timer.timerSpeed = 0.8f
    }
}