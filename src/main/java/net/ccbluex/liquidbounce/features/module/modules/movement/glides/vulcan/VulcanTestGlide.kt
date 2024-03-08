package net.ccbluex.liquidbounce.features.module.modules.movement.glides.vulcan

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode

class VulcanTestGlide : GlideMode("VulcanTest") {
    private var ticks = 0
    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.fallDistance > 0) {
            mc.thePlayer.motionY = -if (mc.thePlayer.ticksExisted % 2 == 0) {
                0.17
            } else {
                0.10
            }
        }
    }
}
