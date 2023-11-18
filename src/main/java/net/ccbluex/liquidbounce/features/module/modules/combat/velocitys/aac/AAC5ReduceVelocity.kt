package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class AAC5ReduceVelocity : VelocityMode("AAC5Reduce") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime> 1 && velocity.velocityInput) {
            mc.thePlayer.motionX *= 0.81
            mc.thePlayer.motionZ *= 0.81
        }
        if (velocity.velocityInput && (mc.thePlayer.hurtTime <5 || mc.thePlayer.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        velocity.velocityInput = true
    }
}