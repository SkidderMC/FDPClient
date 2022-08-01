package net.ccbluex.liquidbounce.features.module.modules.movement.glides.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode

class AAC4XGlide : GlideMode("AAC4.X") {
    private var delay = 0

    override fun onEnable() {
        delay = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround && !mc.thePlayer.isCollided) {
            mc.timer.timerSpeed = 0.6f
            if (mc.thePlayer.motionY < 0 && delay > 0) {
                delay--
                mc.timer.timerSpeed = 0.95f
            } else {
                delay = 0
                mc.thePlayer.motionY = mc.thePlayer.motionY / 0.9800000190734863
                mc.thePlayer.motionY += 0.03
                mc.thePlayer.motionY *= 0.9800000190734863
                mc.thePlayer.jumpMovementFactor = 0.03625f
            }
        } else {
            mc.timer.timerSpeed = 1.0f
            delay = 2
        }
    }
}