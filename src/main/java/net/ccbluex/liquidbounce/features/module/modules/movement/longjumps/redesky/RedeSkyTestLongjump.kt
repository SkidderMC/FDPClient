package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class RedeSkyTestLongjump : LongJumpMode("RedeSkyTest") {
    private var canBoost = false
    
    override fun onEnable() {
        canBoost = false
        sendLegacy()
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (!canBoost) return
        mc.thePlayer.motionY = 0.42
        MovementUtils.strafe(MovementUtils.getSpeed() * 1.12f)
        mc.timer.timerSpeed = 0.8f
    }
    
    override fun onJump(event: JumpEvent) {
        canBoost = true
        event.cancelEvent()
    }
    
    override fun onAttemptJump() {
        mc.thePlayer.jump()
    }
    
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
