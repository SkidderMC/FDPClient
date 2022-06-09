package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue

class VerusHop : SpeedMode("VerusHop") {
    
    private var wasTimer = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.27f
            wasTimer = true
            MovementUtils.strafe(0.4821f)
            mc.thePlayer.motionY = 0.42
        }
    }
}
