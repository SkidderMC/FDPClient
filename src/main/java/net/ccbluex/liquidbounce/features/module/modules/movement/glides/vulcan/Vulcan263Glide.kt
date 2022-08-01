package net.ccbluex.liquidbounce.features.module.modules.movement.glides.vulcan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode

class Vulcan263Glide : GlideMode("Vulcan2.6.3") {
    private var ticks = 0
    override fun onUpdate(event: UpdateEvent) {
        if(mc.thePlayer.motionY<= -0.10) {
            ticks++
                if(ticks % 2 == 0) {
                    mc.thePlayer.motionY = -0.1
                    mc.thePlayer.jumpMovementFactor = 0.0265f
                }else{
                    mc.thePlayer.motionY = -0.16
                    mc.thePlayer.jumpMovementFactor = 0.0265f
                }
        }else{
            ticks = 0
        }
    }
}
