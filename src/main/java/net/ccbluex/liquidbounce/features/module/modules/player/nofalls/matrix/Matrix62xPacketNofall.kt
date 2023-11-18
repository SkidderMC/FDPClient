package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.matrix

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class Matrix62xPacketNofall : NoFallMode("Matrix6.2.X-Packet") {
    override fun onNoFall(event: UpdateEvent) {
        if(mc.thePlayer.onGround) {
            //mc.timer.timerSpeed = 1f
        } else if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
            nofall.wasTimer = true
            mc.timer.timerSpeed = (mc.timer.timerSpeed * if(mc.timer.timerSpeed < 0.6) { 0.25f } else { 0.5f }).coerceAtLeast(0.2f)
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.netHandler.addToSendQueue(C03PacketPlayer(false))
            mc.thePlayer.fallDistance = 0f
        }
    }
}