package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.aac

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.features.value.FloatValue

class AACFlyJesus : JesusMode("AACFly") {
    private val aacMotionValue = FloatValue("${valuePrefix}Motion", 0.5f, 0.1f, 1f)
    override fun onMove(event: MoveEvent) {
        if (!mc.thePlayer.isInWater) {
            return
        }

        event.y = aacMotionValue.get().toDouble()
        mc.thePlayer.motionY = aacMotionValue.get().toDouble()
    }
}
