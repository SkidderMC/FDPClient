package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.redesky

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue

class RedeSky3Longjump : LongJumpMode("RedeSky3") {
    private val jumpTimeValue = IntegerValue("${valuePrefix}JumpTime", 500, 300, 1500)
    private val boostValue = FloatValue("${valuePrefix}Boost", 1F, 0.3F, 1.5F)
    private val heightValue = FloatValue("${valuePrefix}Height", 1F, 0.3F, 1.5F)
    private val timerValue = FloatValue("${valuePrefix}Timer", 1F, 0.1F, 5F)
    private val timer = MSTimer()
    override fun onEnable() {
        timer.reset()
        sendLegacy()
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(jumpTimeValue.get().toLong())) {
            mc.thePlayer.motionY += heightValue.get() / 10F
            MovementUtils.move(boostValue.get() / 10F)
            mc.timer.timerSpeed = timerValue.get()
        } else {
            mc.timer.timerSpeed = 1F
        }
    }

    override fun onJump(event: JumpEvent) {
        timer.reset()
    }
    
    override fun onAttemptJump() {
        mc.thePlayer.jump()
    }
    
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
