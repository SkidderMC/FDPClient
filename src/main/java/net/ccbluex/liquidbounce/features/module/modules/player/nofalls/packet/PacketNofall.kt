package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.packet

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class PacketNofall : NoFallMode("Packet") {
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            mc.thePlayer.fallDistance = 0f
        }
    }
}