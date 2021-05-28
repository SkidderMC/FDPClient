package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange

class PacketFixer : Listenable,MinecraftInstance() {
    private var serversideSlot=0

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet

        if(packet is C09PacketHeldItemChange){
            if(packet.slotId==serversideSlot)
                event.cancelEvent()

            serversideSlot=packet.slotId
        }else if(packet is S09PacketHeldItemChange){
            serversideSlot=packet.heldItemHotbarIndex
        }else if(packet is C01PacketChatMessage){
            println(event.type)
        }
    }

    override fun handleEvents(): Boolean {
        return true
    }
}