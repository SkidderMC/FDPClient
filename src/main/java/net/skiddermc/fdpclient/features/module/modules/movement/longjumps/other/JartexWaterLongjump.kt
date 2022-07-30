package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.other

import net.skiddermc.fdpclient.event.MotionEvent
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.FloatValue

class JartexWaterLongjump : LongJumpMode("JartexWater") {
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