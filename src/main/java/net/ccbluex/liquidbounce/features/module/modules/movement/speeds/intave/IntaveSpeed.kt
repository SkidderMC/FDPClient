package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils


class IntaveSpeed : SpeedMode("Intave") {

    override fun onUpdate(){
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.thePlayer.jump()
            }
            if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance <= 0.1) {
                mc.timer.timerSpeed = 1.4f
            }
            if (mc.thePlayer.fallDistance > 0.1 && mc.thePlayer.fallDistance < 1.3) {
                mc.timer.timerSpeed = 0.7f
            }
            if (mc.thePlayer.fallDistance >= 1.3) {
                mc.timer.timerSpeed = 1f
            }
        }
    }

}