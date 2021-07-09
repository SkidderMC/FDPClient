package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class RedeSkyGround : SpeedMode("RedeSkyGround") {
    private val timer=MSTimer()
    private var stage=false

    override fun onMotion() {
        if(MovementUtils.isMoving()){
            if(stage){
                mc.timer.timerSpeed=1.5F
                if(timer.hasTimePassed(700)){
                    timer.reset()
                    stage=!stage
                }
            }else{
                mc.timer.timerSpeed=0.8F
                if(timer.hasTimePassed(400)){
                    timer.reset()
                    stage=!stage
                }
            }
        }
    }
}