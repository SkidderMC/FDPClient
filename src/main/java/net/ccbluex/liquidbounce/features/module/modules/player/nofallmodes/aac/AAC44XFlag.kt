package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

object AAC44XFlag : NoFallMode("AAC4.4.X-Flag") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return

        when (val packet = event.packet) {
            is S12PacketEntityVelocity -> {
                if (player.fallDistance > 1.8f) {
                    packet.motionY = (packet.motionY * -0.1).toInt()
                }
            }

            is C03PacketPlayer -> {
                if (player.fallDistance > 1.6f) {
                    packet.onGround = true
                }
            }
        }
    }
}
