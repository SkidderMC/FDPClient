package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue

class RedeSkyLongjump : LongJumpMode("RedeSky") {
    private val jumpMovementValue = FloatValue("${valuePrefix}JumpMovement", 0.13F, 0.05F, 0.25F)
    private val motionYValue = FloatValue("${valuePrefix}MotionY", 0.81F, 0.05F, 1F)
    private val moveReducerValue = BoolValue("${valuePrefix}MovementReducer", true)
    private val reduceMovementValue = FloatValue("${valuePrefix}ReduceMovement", 0.08F, 0.05F, 0.25F).displayable { moveReducerValue.get() }
    private val motYReducerValue = BoolValue("${valuePrefix}MotionYReducer", true)
    private val reduceYMotionValue = FloatValue("${valuePrefix}ReduceYMotion", 0.15F, 0.01F, 0.20F).displayable { motYReducerValue.get() }
    private val useTimerValue = BoolValue("${valuePrefix}Timer", true)
    private val timerValue = FloatValue("${valuePrefix}Timer", 0.30F, 0.1F, 1F).displayable { useTimerValue.get() }

    override fun onEnable() {
        sendLegacy()
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            if (moveReducerValue.get()) {
                mc.thePlayer.jumpMovementFactor = jumpMovementValue.get() - (longjump.airTick * (reduceMovementValue.get() / 100))
            } else {
                mc.thePlayer.jumpMovementFactor = jumpMovementValue.get()
            }
            if (motYReducerValue.get()) {
                mc.thePlayer.motionY += (motionYValue.get() / 10F) - (longjump.airTick * (reduceYMotionValue.get() / 100))
            } else {
                mc.thePlayer.motionY += motionYValue.get() / 10F
            }
            if (useTimerValue.get()) {
                mc.timer.timerSpeed = timerValue.get()
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
