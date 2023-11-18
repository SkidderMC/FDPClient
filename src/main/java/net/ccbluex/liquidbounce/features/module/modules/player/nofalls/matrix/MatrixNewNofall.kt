package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
Thx To Zerolysimin#6403
 */

class MatrixNewNofall : NoFallMode("MatrixNew") {

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            if(!mc.thePlayer.onGround) {
                if(mc.thePlayer.fallDistance > 2.69f){
                    mc.timer.timerSpeed = 0.3f
                    event.packet.onGround = true
                    mc.thePlayer.fallDistance = 0f
                }
                if(mc.thePlayer.fallDistance > 3.5){
                    mc.timer.timerSpeed = 0.3f
                }else {
                    mc.timer.timerSpeed = 1F
                }
            }
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0))
                    .isNotEmpty()) {
                if(!event.packet.isOnGround && mc.thePlayer.motionY < -0.6) {
                    event.packet.onGround = true
                }
            }

        }
    }
}