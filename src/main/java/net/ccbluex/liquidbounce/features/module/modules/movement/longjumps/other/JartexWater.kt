package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class JartexWater : LongJumpMode("JartexWater") {
    private val hValue = FloatValue("${valuePrefix}Horizon", 1.0f, 0.8f, 4.0f)
    private val yValue = FloatValue("${valuePrefix}MotionY", 0.42f, 0.0f, 2.0f)
    override fun onPreMotion(event: MotionEvent) {
        if(mc.thePlayer.isInWater) {
            mc.thePlayer.motionY = yValue.get().toDouble()
            MovementUtils.strafe(hValue.get())
            longjump.hasJumped = true
        }
    }
}