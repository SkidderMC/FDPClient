package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.value.*

class RedeSkyLongjump : LongJumpMode("RedeSky") {
    private val jumpMovementValue = FloatValue("${valuePrefix}JumpMovement", 0.13F, 0.05F, 0.25F)
    private val motionYValue = FloatValue("${valuePrefix}MotionY", 0.81F, 0.05F, 1F)
    private val moveReducerValue = BoolValue("${valuePrefix}MovementReducer", true)
    private val reduceMovementValue = FloatValue("${valuePrefix}ReduceMovement", 0.08F, 0.05F, 0.25F)
    private val motYReducerValue = BoolValue("${valuePrefix}MotionYReducer", true)
    private val reduceYMotionValue = FloatValue("${valuePrefix}ReduceYMotion", 0.15F, 0.01F, 0.20F)
    private val useTimerValue = BoolValue("${valuePrefix}Timer", true)
    private val timerValue = FloatValue("${valuePrefix}Timer", 0.30F, 0.1F, 1F)
    private var airTicks = 0
    override fun onEnable() {
        airTicks = 0
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            airTicks++
        } else {
            airTicks = 0
        }
        if (!mc.thePlayer.onGround) {
            if (moveReducerValue.get()) {
                mc.thePlayer.jumpMovementFactor = jumpMovementValue.get() - (airTicks * (reduceMovementValue.get() / 100))
            } else {
                mc.thePlayer.jumpMovementFactor = jumpMovementValue.get()
            }
            if (motYReducerValue.get()) {
                mc.thePlayer.motionY += (motionYValue.get() / 10F) - (airTicks * (reduceYMotionValue.get() / 100))
            } else {
                mc.thePlayer.motionY += motionYValue.get() / 10F
            }
            if (useTimerValue.get()) {
                mc.timer.timerSpeed = timerValue.get()
            }
        }
    }
}