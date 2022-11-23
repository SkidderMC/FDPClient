package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class SpoofVelocity : VelocityMode("Spoof") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / 8000.0, mc.thePlayer.posY + packet.motionY / 8000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
            mc.timer.timerSpeed = 0.6f
            velocity.wasTimer = true
        }
    }
}