package net.skiddermc.fdpclient.features.module.modules.movement.flys.other

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils

class WatchCatFly : FlyMode("WatchCat") {
    override fun onUpdate(event: UpdateEvent) {
        MovementUtils.strafe(0.15F)
        mc.thePlayer.isSprinting = true

        if (mc.thePlayer.posY < fly.launchY + 2) {
            mc.thePlayer.motionY = Math.random() * 0.5
            return
        }

        if (fly.launchY > mc.thePlayer.posY) {
            MovementUtils.strafe(0F)
        }
    }
}