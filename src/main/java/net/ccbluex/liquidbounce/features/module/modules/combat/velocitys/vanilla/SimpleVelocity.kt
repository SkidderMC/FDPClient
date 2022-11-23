package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class SimpleVelocity : VelocityMode("Simple") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            val horizontal = velocity.horizontalValue.get()
            val vertical = velocity.verticalValue.get()

            if (horizontal == 0F && vertical == 0F) {
                event.cancelEvent()
            }

            packet.motionX = (packet.getMotionX() * horizontal).toInt()
            packet.motionY = (packet.getMotionY() * vertical).toInt()
            packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
        }
    }
}