package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class NewRedeSkyGround : SpeedMode("NewRedeSkyGround") {
    private val timer=MSTimer()
    private var stage=false

    override fun onMotion() {
        if(MovementUtils.isMoving()){
            if(stage){
                mc.timer.timerSpeed=1.20F
                if(timer.hasTimePassed(600)){
                    timer.reset()
                    stage=!stage
                }
            }else{
                mc.timer.timerSpeed=0.85F
                if(timer.hasTimePassed(400)){
                    timer.reset()
                    stage=!stage
                }
            }
        }
    }
}
