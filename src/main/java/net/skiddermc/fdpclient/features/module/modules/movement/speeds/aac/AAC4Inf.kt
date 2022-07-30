package net.skiddermc.fdpclient.features.module.modules.movement.speeds.aac

import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils

class AAC4Inf : SpeedMode("AAC4Inf") {
    override fun onUpdate() {
        mc.timer.timerSpeed = 1.00f
        if (!MovementUtils.isMoving()) {
            return
        }
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            mc.timer.timerSpeed = 1.00f
        }
        if (mc.thePlayer.fallDistance > 0.7 && mc.thePlayer.fallDistance < 1.3) {
            mc.timer.timerSpeed = 1.08f
        }
    }

    override fun onDisable() {
        mc.thePlayer!!.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
    }
}
