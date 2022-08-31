package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.client.C03PacketPlayer
/**
Thx To Zerolysimin#6403
 */
class MatrixCollideNofall : NoFallMode("MatrixCollide") {
    private var packet1Count = 0
    private var packetModify = false
    private var needSpoof = false
    override fun onEnable() {
        needSpoof = false
        packetModify = false
        packet1Count = 0
    }
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance.toInt() - mc.thePlayer.motionY > 3) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.fallDistance = 0.0f
            mc.thePlayer.motionX *= 0.1
            mc.thePlayer.motionZ *= 0.1
            needSpoof = true
        }

        if (mc.thePlayer.fallDistance / 3 > packet1Count) {
            packet1Count = mc.thePlayer.fallDistance.toInt() / 3
            packetModify = true
        }
        if (mc.thePlayer.onGround) {
            packet1Count = 0
        }

    }
    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer && needSpoof) {
            event.packet.onGround = true
            needSpoof = false
        }
    }
}