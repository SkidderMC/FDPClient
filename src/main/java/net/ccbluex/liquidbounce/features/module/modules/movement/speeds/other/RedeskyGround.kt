package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class RedeskyGround : SpeedMode("RedeskyGround") {
    private val timer = MSTimer()
    private var stage = false

    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            if (stage) {
                mc.timer.timerSpeed = 1.20F
                if (timer.hasTimePassed(600)) {
                    timer.reset()
                    stage = !stage
                }
            } else {
                mc.timer.timerSpeed = 0.85F
                if (timer.hasTimePassed(400)) {
                    timer.reset()
                    stage = !stage
                }
            }
        }
    }
}