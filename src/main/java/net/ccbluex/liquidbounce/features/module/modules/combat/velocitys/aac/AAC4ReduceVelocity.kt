package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class AAC4ReduceVelocity : VelocityMode("AAC4Reduce") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime> 0 && !mc.thePlayer.onGround && velocity.velocityInput && velocity.velocityTimer.hasTimePassed(80L)) {
            mc.thePlayer.motionX *= 0.62
            mc.thePlayer.motionZ *= 0.62
        }
        if (velocity.velocityInput && (mc.thePlayer.hurtTime <4 || mc.thePlayer.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
            velocity.velocityInput = false
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            velocity.velocityInput = true
            packet.motionX = (packet.getMotionX() * 0.6).toInt()
            packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
        }
    }
}