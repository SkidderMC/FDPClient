package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VerusYPort : SpeedMode("VerusYPort") {
    override fun onUpdate() {
        if (mc.thePlayer!!.isInWater) return
        if (MovementUtils.isMoving() && mc.thePlayer!!.onGround) {
	mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ);
	MovementUtils.strafe(0.6075)
        }else if(MovementUtils.isMoving()) MovementUtils.strafe()
	else {
	    mc.thePlayer.motionZ = 0.0
	    mc.thePlayer.motionX = 0.0
        }
    }

    override fun onDisable() {

    }
}