package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class VerusNofall : NoFallMode("Verus") {
    private var needSpoof = false
    private var packetModify = false
    private var packet1Count = 0
    override fun onEnable() {
        needSpoof = false
        packetModify = false
        packet1Count = 0
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer && needSpoof) {
            event.packet.onGround = true
            needSpoof = false
        }
    }
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.fallDistance = 0.0f
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
            needSpoof = true
        }

        if (mc.thePlayer.fallDistance.toInt() / 3 > packet1Count) {
            packet1Count = mc.thePlayer.fallDistance.toInt() / 3
            packetModify = true
        }
        if (mc.thePlayer.onGround) {
            packet1Count = 0
        }
    }
}