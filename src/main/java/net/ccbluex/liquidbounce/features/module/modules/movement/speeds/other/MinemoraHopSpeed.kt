package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MinemoraHopSpeed : SpeedMode("MinemoraHop") {

    private var movetick = 0
    override fun onEnable() {
        movetick = 0
    }
    override fun onPreMotion() {
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving() || mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindBack.pressed || mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                movetick = 0
            } else {
                movetick++
                when(movetick) {
                    1 -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.011f)
                        mc.timer.timerSpeed = 0.98f
                    }
                    2 -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.01f)
                        mc.timer.timerSpeed = 1.01f
                    }
                    3 -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.012f)
                        mc.timer.timerSpeed = 0.9f
                    }
                    4 -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1f)
                        mc.timer.timerSpeed = 1.2f
                    }
                    5 -> {
                        movetick = 0
                        mc.timer.timerSpeed = 1.0f
                    }
                }
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
