package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class VulcanYPort2Speed : SpeedMode("VulcanYPort2") {
	
    private var wasTimer = false
    private var portSwitcher = 0
  
    override fun onEnable() {
        wasTimer = true
        mc.timer.timerSpeed = 1.0f
        portSwitcher = 0
    }
    
    override fun onDisable() {
        wasTimer = false
        mc.timer.timerSpeed = 1.0f
        portSwitcher = 0
    }

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.0f
            wasTimer = false
        }
        if (portSwitcher > 1) {
            mc.thePlayer.motionY = -0.2784
            mc.timer.timerSpeed = 1.5f
            wasTimer = true
            if(portSwitcher > 1) {
                portSwitcher = 0
            }
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            MovementUtils.strafe()
            if(portSwitcher >= 1) {
                mc.thePlayer.motionY = 0.2
                mc.timer.timerSpeed = 1.5f
            }
            portSwitcher++
        }else if(MovementUtils.getSpeed() < 0.225){
            MovementUtils.strafe(0.225f)
        }
    }
}
