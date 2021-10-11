package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode

class HACFly : FlyMode("HAC") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionX *= 0.8
        mc.thePlayer.motionZ *= 0.8
        mc.thePlayer.motionY = if (mc.thePlayer.motionY <= -0.42) { 0.42 } else { -0.42 }
    }
}
