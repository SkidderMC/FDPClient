

// file was commented out because build failed


/* package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer

class PikaNetworkNofall : NoFallMode("PikaNetwork") {
    private var matrixSend = false
    override fun onEnable() {
        matrixSend = false
    }
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
            mc.thePlayer.fallDistance = 0.0f
            matrixSend = true
            mc.timer.timerSpeed = 1.0f
            nofall.wasTimer = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && matrixSend) {
            matrixSend = false
            event.cancelEvent()
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(event.packet.posX, event.packet.posY, event.packet.posZ, true))
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(event.packet.posX, event.packet.posY, event.packet.posZ, false))
        }

    }
}
*/
