package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer

class HypixelFlagNofall : NoFallMode("HypixelFlag") {
    
    private var sendPacket = false
    
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            sendPacket = false
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (packet is C03PacketPlayer) {
            val fallingPlayer = FallingPlayer(mc.thePlayer)
            val collLoc = fallingPlayer.findCollision(60) // null -> too far to calc or fall pos in void
            if (abs((collLoc?.y ?: 0) - mc.thePlayer.posY) < 3 && mc.thePlayer.fallDistance > 4 && !sendPacket) {
                event.cancelEvent()
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, false))
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z - 23, true))
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, false))

                sendPacket = true
            }
            }
    }
}
