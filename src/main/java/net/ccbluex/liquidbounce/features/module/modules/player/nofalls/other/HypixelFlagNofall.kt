package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class HypixelFlagNofall : NoFallMode("HypixelFlag") {
    private var isDmgFalling = false
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance > 3) {
            isDmgFalling = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            if(isDmgFalling) {
                if (event.packet.onGround && mc.thePlayer.onGround) {
                    isDmgFalling = false
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z - 23, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, false))
                }
            }
        }
    }
}