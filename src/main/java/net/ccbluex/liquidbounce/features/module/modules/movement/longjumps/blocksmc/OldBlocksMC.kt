package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.blocksmc

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class OldBlocksMC : LongJumpMode("OldBlocksMC") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.jumpMovementFactor = 0.1f
        mc.thePlayer.motionY += 0.0132
        mc.thePlayer.jumpMovementFactor = 0.09f
        mc.timer.timerSpeed = 0.8f
        MovementUtils.strafe()
    }
}