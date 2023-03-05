package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue

class RedeSky2Longjump : LongJumpMode("RedeSky2") {
    private val airSpeedValue = FloatValue("${valuePrefix}AirSpeed", 0.1F, 0.05F, 0.25F)
    private val minAirSpeedValue = FloatValue("${valuePrefix}MinAirSpeed", 0.08F, 0.05F, 0.25F)
    private val airSpeedReducerValue = BoolValue("${valuePrefix}AirSpeedReducer", true)
    private val reduceAirSpeedValue = FloatValue("${valuePrefix}ReduceAirSpeed", 0.16F, 0.05F, 0.25F).displayable { airSpeedReducerValue.get() }
    private val yMotionValue = FloatValue("${valuePrefix}YMotion", 0.08F, 0.01F, 0.20F)
    private val minYMotionValue = FloatValue("${valuePrefix}MinYMotion", 0.04F, 0.01F, 0.20F)
    private val yMotionReducerValue = BoolValue("${valuePrefix}YMotionReducer", true)
    private val reduceYMotionValue = FloatValue("${valuePrefix}ReduceYMotion", 0.15F, 0.01F, 0.20F).displayable { yMotionReducerValue.get() }

    override fun onEnable() {
        sendLegacy()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            if (yMotionReducerValue.get()) {
                val motY = yMotionValue.get() - (longjump.airTick * (reduceYMotionValue.get() / 100))
                if (motY < minYMotionValue.get()) {
                    mc.thePlayer.motionY += minYMotionValue.get()
                } else {
                    mc.thePlayer.motionY += motY
                }
            } else {
                mc.thePlayer.motionY += yMotionValue.get()
            }
            
            if (airSpeedReducerValue.get()) {
                val airSpeed = airSpeedValue.get() - (longjump.airTick * (reduceAirSpeedValue.get() / 100))
                if (airSpeed < minAirSpeedValue.get()) {
                    mc.thePlayer.speedInAir = minAirSpeedValue.get()
                } else {
                    mc.thePlayer.speedInAir = airSpeed
                }
            } else {
                mc.thePlayer.speedInAir = airSpeedValue.get()
            }
        }
    }
    
    override fun onAttemptJump() {
        mc.thePlayer.jump()
    }
    
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
