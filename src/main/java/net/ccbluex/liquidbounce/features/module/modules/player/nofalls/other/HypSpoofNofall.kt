package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer

class HypSpoofNofall : NoFallMode("HypSpoof") {
    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, true))
    }
}