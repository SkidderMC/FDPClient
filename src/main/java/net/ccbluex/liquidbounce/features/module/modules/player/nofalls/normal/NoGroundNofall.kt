package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.normal

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class NoGroundNofall : NoFallMode("NoGround") {
    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) event.packet.onGround = false
    }
}