package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixSpoofVelocity : VelocityMode("MatrixSpoof") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / - 24000.0, mc.thePlayer.posY + packet.motionY / -24000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
        }
    }
}