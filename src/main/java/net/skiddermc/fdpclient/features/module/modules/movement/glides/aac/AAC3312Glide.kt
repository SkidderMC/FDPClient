package net.skiddermc.fdpclient.features.module.modules.movement.glides.aac

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.glides.GlideMode

class AAC3312Glide : GlideMode("AAC3.3.12") {
    private var delay = 0

    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            delay++
        }

        if (delay == 2) {
            mc.timer.timerSpeed = 1F
        }

        if (delay == 12) {
            mc.timer.timerSpeed = 0.1F
        }

        if (delay >= 12 && !mc.thePlayer.onGround) {
            delay = 0
            mc.thePlayer.motionY = .015
        }
    }
}