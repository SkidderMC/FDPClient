package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanHop : SpeedMode("VulcanHop") {

    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = if((mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindBack.pressed)
            && (mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed)) {
            0.0263f
        } else {
            0.0265f
        }
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
        }
        if (MovementUtils.getSpeed() < 0.215) {
            MovementUtils.strafe()
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            mc.thePlayer.motionX *= 0.99
            mc.thePlayer.motionZ *= 0.99 // Speed [C] checks
        }
    }
}