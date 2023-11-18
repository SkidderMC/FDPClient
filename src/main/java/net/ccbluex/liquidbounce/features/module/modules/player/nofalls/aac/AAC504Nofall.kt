package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class AAC504Nofall : NoFallMode("AAC5.0.4") {
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
                    event.packet.onGround = true
                    mc.thePlayer.onGround = false
                    event.packet.y += 1.0
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y - 1.0784, event.packet.z, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y - 0.5, event.packet.z, true))
                }
            }
        }
    }
}