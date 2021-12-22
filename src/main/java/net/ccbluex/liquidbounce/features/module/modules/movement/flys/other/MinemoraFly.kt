package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode

class MinemoraFly : FlyMode("Minemora") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY = -0.0784000015258789
    }
}