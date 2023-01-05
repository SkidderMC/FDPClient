package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AACv1Longjump : LongJumpMode("AACv1") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.05999
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.08f)
    }
    override fun onAttemptJump() {
        mc.thePlayer.jump()
    }
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
