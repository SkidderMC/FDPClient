package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils

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