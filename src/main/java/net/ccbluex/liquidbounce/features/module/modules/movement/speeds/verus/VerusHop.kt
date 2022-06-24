package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue

class VerusHop : SpeedMode("VerusHop") {

    private val upTimer = BoolValue("${valuePrefix}-upTimer", true)

    private var wasTimer = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()

                if (!mc.thePlayer.isAirBorne) {
                    return //Prevent flag with Fly
                }
                if (upTimer.get()) {
                    mc.timer.timerSpeed = 1.27f
                }
                wasTimer = true
                MovementUtils.strafe(0.4848f)
            }
            MovementUtils.strafe()
        }
    }
}
