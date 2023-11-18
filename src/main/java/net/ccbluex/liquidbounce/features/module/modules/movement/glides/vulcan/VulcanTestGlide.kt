package net.ccbluex.liquidbounce.features.module.modules.movement.glides.vulcan

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode

class VulcanTestGlide : GlideMode("VulcanTest") {
    private var ticks = 0
    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.fallDistance > 2) {
            mc.thePlayer.onGround = true
            mc.thePlayer.fallDistance = 0f
        }
        if (mc.thePlayer.ticksExisted % 3 == 0) {
            mc.thePlayer.motionY += 0.026
        } else {
            mc.thePlayer.motionY = -0.0991
        }
    }
}
