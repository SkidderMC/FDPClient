package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.packet

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object Packet1 : NoFallMode("Packet1") {
    private var packetCount = 0
    private var packetModify = false

    override fun onEnable() {
        packetCount = 0
        packetModify = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance.toInt() / 3 > packetCount) {
            packetCount = player.fallDistance.toInt() / 3
            packetModify = true
        }

        if (player.onGround) {
            packetCount = 0
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && packetModify) {
            packet.onGround = true
            packetModify = false
        }
    }
}
