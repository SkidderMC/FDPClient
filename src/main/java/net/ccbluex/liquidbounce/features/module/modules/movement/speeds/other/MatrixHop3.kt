package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixHop3 : SpeedMode("Matrix6.7.0") {

    override fun onUpdate() {
        if(!mc.thePlayer.onGround) {
			if(mc.thePlayer.motionY > 0) {
				mc.thePlayer.motionY -= 0.0005
			}
			mc.thePlayer.motionY -= 0.0094001145141919810
		}
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            if (MovementUtils.getSpeed() < 0.218) {
                MovementUtils.strafe(0.218f)
            }
        }
        if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
            mc.thePlayer.jumpMovementFactor = 0.02605f
        }else{
            mc.thePlayer.jumpMovementFactor = 0.025f
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.41050001145141919810
            if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
                MovementUtils.strafe((MovementUtils.getSpeed() * 1.00299601145141919810).toFloat())
            }
        }
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}
