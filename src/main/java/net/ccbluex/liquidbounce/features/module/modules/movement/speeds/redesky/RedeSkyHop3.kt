package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class RedeSkyHop3 : SpeedMode("RedeSkyHop3") {
    override fun onMotion() {
        if(MovementUtils.isMoving()) {
            val speedModule=LiquidBounce.moduleManager.getModule(Speed::class.java)
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY=speedModule.redeSkyHeight.get().toDouble()
            } else {
                mc.thePlayer.jumpMovementFactor = speedModule.redeSkyHop3Speed.get()
            }
        }
    }
}