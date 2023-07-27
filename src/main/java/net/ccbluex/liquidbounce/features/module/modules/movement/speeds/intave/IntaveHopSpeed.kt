package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.features.value.BoolValue

class IntaveHopSpeed : SpeedMode("IntaveHop") {
    private val groundStrafe = BoolValue("${valuePrefix}Strafe", false)

    override fun onUpdate() {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.timer.timerSpeed = 1.0f
                if (groundStrafe.get()) MovementUtils.strafe()
                mc.thePlayer.jump()
            }
            
             if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0014
                mc.thePlayer.motionZ *= 1.0016
                mc.timer.timerSpeed = 1.07f
             }
        }
       
    }
}
