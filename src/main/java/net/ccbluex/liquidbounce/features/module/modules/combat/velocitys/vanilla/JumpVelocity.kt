package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class JumpVelocity : VelocityMode("Jump") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42
        }
    }
}