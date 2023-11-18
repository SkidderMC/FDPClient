package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanLowHopSpeed : SpeedMode("VulcanLowHop") {
    private var ticks = 0
    private var launchY = 0.0

    override fun onUpdate() {
        ticks++
        mc.thePlayer.jumpMovementFactor = 0.0245f
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            ticks = 0
            MovementUtils.strafe()
            if (MovementUtils.getSpeed() < 0.5f) {
                MovementUtils.strafe(0.484f)
            }
            launchY = mc.thePlayer.posY
        }else if (mc.thePlayer.posY > launchY && ticks <= 1) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, launchY, mc.thePlayer.posZ)
        }else if (ticks == 5) {
            mc.thePlayer.motionY = -0.17
        }
        if (MovementUtils.getSpeed() < 0.215) {
            MovementUtils.strafe(0.215f)
        }
    }

    override fun onDisable() {
        ticks = 0
        mc.timer.timerSpeed = 1f
    }
    
    override fun onEnable() {
        ticks = 0
        mc.timer.timerSpeed = 1f
        launchY = mc.thePlayer.posY
    }
}
