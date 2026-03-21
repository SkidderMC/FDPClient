package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.normal

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object Vanilla : NoFallMode("Vanilla") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && player.fallDistance > 2.5f) {
            packet.onGround = true
        }
    }
}
