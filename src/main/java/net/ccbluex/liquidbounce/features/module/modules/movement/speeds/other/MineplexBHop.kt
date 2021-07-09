package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MineplexBHop : SpeedMode("MineplexBHop") {
    private var boost = 0
    private var jumped=false

    override fun onEnable() {
        jumped=false
        boost=0
    }

    override fun onUpdate() {
        if(!MovementUtils.isMoving()){
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            return
        }

        if(jumped) {
            if(mc.thePlayer.onGround) {
                jumped=false
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                return
            }
            MovementUtils.strafe(MovementUtils.getSpeed() * when(boost){
                0 -> 1.75f
                else -> 1f
            })
            boost++
        }else if(mc.thePlayer.onGround){
            boost=0
            mc.thePlayer.jump()
            jumped=true
        }

        if(mc.thePlayer.fallDistance>1.5){
            mc.thePlayer.jumpMovementFactor = 0.01f
        }else if(mc.thePlayer.fallDistance>0){
            mc.thePlayer.jumpMovementFactor = 0.035f
            mc.thePlayer.motionY += 0.02
        }else{
            mc.thePlayer.jumpMovementFactor = 0.025f
        }
    }
}