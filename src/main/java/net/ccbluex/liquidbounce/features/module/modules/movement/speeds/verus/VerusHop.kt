package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue

class VerusHop : SpeedMode("VerusHop") {

    private val timerBoost = BoolValue("TimerBoost",true)
        
    private var jumps = 0
    private var lastY = 0.0

    override fun onPreMotion() {
        
        if (MovementUtils.isMoving()) {
            if (timerBoost.get() && (jumps >= 1)) {
                mc.timer.timerSpeed = if (mc.thePlayer.motionY < 0) { 0.88f } else { 1.25f }
            }
            
            when {
                mc.thePlayer.onGround -> {
                    MovementUtils.strafe(0.4825f)
                    mc.thePlayer.motionY = (0.42f).toDouble()
                    
                    if (mc.thePlayer.posY == lastY) {
                        jumps++
                    } else {
                        jumps = 0
                    }
                    
                    lastY = mc.thePlayer.posY
                }
                else -> {
                    MovementUtils.strafe(0.334f)
                }
            }
        }
    }
}
