package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class GlitchVelocity : VelocityMode("Glitch") {
    override fun onVelocity(event: UpdateEvent) {
        mc.thePlayer.noClip = velocity.velocityInput

        if (mc.thePlayer.hurtTime == 7) {
            mc.thePlayer.motionY = 0.41999998688698
        }

        velocity.velocityInput = false
    }

    override fun onVelocityPacket(event: PacketEvent) {
        if (!mc.thePlayer.onGround) {
            return
        }

        velocity.velocityInput = true
        event.cancelEvent()
    }
}
