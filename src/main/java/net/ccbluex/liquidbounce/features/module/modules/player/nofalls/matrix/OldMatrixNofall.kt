package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class OldMatrixNofall : NoFallMode("OldMatrix") {
    private var isDmgFalling = false
    private var matrixFlagWait = 0
    override fun onEnable() {
        isDmgFalling = false
        matrixFlagWait = 0
    }

    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance > 3) {
            isDmgFalling = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is S08PacketPlayerPosLook && matrixFlagWait > 0) {
            matrixFlagWait = 0
            mc.timer.timerSpeed = 1.00f
            event.cancelEvent()
        }
        if(event.packet is C03PacketPlayer && isDmgFalling) {
            if (event.packet.onGround && mc.thePlayer.onGround) {
                matrixFlagWait = 2
                isDmgFalling = false
                event.cancelEvent()
                mc.thePlayer.onGround = false
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, event.packet.y - 256, event.packet.z, false))
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(event.packet.x, (-10).toDouble() , event.packet.z, true))
                mc.timer.timerSpeed = 0.18f
            }
        }
    }
}