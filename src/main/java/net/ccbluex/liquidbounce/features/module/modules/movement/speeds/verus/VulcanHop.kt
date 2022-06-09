package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VulcanHop : SpeedMode("VulcanHop") {

    override fun onUpdate() {
        mc.timer.timerSpeed = 1.00f
        if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1f) {
            mc.thePlayer.jumpMovementFactor = 0.026499f
        }else {
            mc.thePlayer.jumpMovementFactor = 0.0244f
        }
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
        }
        if (MovementUtils.getSpeed() < 0.215f) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.25f
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
	    if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            MovementUtils.strafe()
	    if(MovementUtils.getSpeed() < 0.5f) {
	        MovementUtils.strafe(0.4849f)
	    }
        }else if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
