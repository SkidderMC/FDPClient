package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode

class MinemoraGlide : GlideMode("Minemora") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY = -0.0784000015258789
    }
}