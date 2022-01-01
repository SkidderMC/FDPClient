package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixBHop : SpeedMode("MatrixBHop") {
    private val speedMultiValue = FloatValue("MatrixBHopSpeed", 1f, 0.7f, 1.2f)
    private var recX = 0.0
    private var recY = 0.0
    private var recZ = 0.0
    private var jumped = false
    private var dist = 0.0
    override fun onUpdate() {
        if (!mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1.0f
        }
        if (jumped) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            jumped = false
            mc.timer.timerSpeed = 0.9f
	    dist = Math.sqrt((recX-mc.thePlayer.posX)*(recX-mc.thePlayer.posX) + (recZ-mc.thePlayer.posZ)*(recZ-mc.thePlayer.posZ))
	    if(MovementUtils.getSpeed() > 0 ) {
	        var recSpeed = MovementUtils.getSpeed()
		mc.thePlayer.motionX *= (0.912*dist*speedMultiValue.get())/recSpeed
		mc.thePlayer.motionZ *= (0.912*dist*speedMultiValue.get())/recSpeed
	    }
        }else if(!mc.thePlayer.onGround) {
	    mc.thePlayer.jumpMovementFactor = 0.0265f
	}
        if (mc.thePlayer.onGround && MovementUtils.isMoving() && !mc.thePlayer.isInWater) {
            recX = mc.thePlayer.posX
	    recY = mc.thePlayer.posY
	    recZ = mc.thePlayer.posZ
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            jumped = true
            mc.timer.timerSpeed = 2.0f
        }
    }

    override fun onDisable() {
        jumped = false
        mc.timer.timerSpeed = 1f
        mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
    }
}
