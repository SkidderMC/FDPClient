package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

class SimpleVelocity : VelocityMode("Simple") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if(RandomUtils.nextInt(1,100) <= velocity.chanceValue.get()){
                val horizontal = velocity.horizontalValue.get()
                val vertical = velocity.verticalValue.get()

                if (horizontal == 0F && vertical == 0F) {
                    event.cancelEvent()
                    return
                }

                if (horizontal == 0F) {
                    mc.thePlayer.motionY = packet.getMotionY().toDouble() * vertical / 8000.0
                    event.cancelEvent()
                    return
                }

                packet.motionX = (packet.getMotionX() * horizontal).toInt()
                packet.motionY = (packet.getMotionY() * vertical).toInt()
                packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
            }
        }
    }
}
