package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.spartan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class SpartanLowHopSpeed : SpeedMode("SpartanLowHop") {
    private var ticks = 0
    private var launchY = 0.0

    override fun onUpdate() {
        ticks++
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            ticks = 0
            MovementUtils.strafe(0.48f)
            launchY = mc.thePlayer.posY
        }else if (mc.thePlayer.posY > launchY) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, launchY, mc.thePlayer.posZ)
        }
        if (MovementUtils.getSpeed() < 0.225) {
            MovementUtils.strafe(0.225f)
        }else {
            MovementUtils.strafe()
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
