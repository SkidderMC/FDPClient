package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object MatrixNew : NoFallMode("MatrixNew") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if (!player.onGround) {
                if (player.fallDistance > 2.69f) {
                    mc.timer.timerSpeed = 0.3f
                    packet.onGround = true
                    player.fallDistance = 0f
                }

                mc.timer.timerSpeed = if (player.fallDistance > 3.5f) 0.3f else 1f
            }

            if (mc.theWorld.getCollidingBoundingBoxes(player, player.entityBoundingBox.offset(0.0, player.motionY, 0.0))
                    .isNotEmpty()
            ) {
                if (!packet.onGround && player.motionY < -0.6) {
                    packet.onGround = true
                    player.onGround = true
                }
            }
        }
    }
}
