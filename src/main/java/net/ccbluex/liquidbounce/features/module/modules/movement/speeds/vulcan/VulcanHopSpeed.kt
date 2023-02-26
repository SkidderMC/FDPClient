package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.client.settings.GameSettings

class VulcanHopSpeed : SpeedMode("VulcanHop") {
    
    private var wasTimer = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if ((RotationUtils.targetRotation == null && Math.abs(mc.thePlayer.moveStrafing) < 0.1) || (RotationUtils.targetRotation != null && Math.abs(RotationUtils.getAngleDifference(MovementUtils.movingYaw, RotationUtils.targetRotation.yaw)) < 45.0f)) {
            mc.thePlayer.jumpMovementFactor = 0.026499f
        }else {
            mc.thePlayer.jumpMovementFactor = 0.0244f
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)

        if (MovementUtils.getSpeed() < 0.215f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.25f
            wasTimer = true
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
