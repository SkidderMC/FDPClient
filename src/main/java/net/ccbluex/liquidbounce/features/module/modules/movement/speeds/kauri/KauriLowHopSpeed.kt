package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.kauri

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class KauriLowHopSpeed : SpeedMode("KauriLowHop") {
    private var ticks = 0

    override fun onUpdate() {
        ticks++
        if (!mc.thePlayer.onGround && ticks == 3) {
            mc.thePlayer.motionY = 0.00
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.3001145141919810
            if (MovementUtils.getSpeed() < 0.22) {
                MovementUtils.strafe(0.22f)
            }else {
                MovementUtils.strafe(0.48f)
            }
        }
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onDisable() {
        ticks = 0
        mc.timer.timerSpeed = 1f
    }
}
