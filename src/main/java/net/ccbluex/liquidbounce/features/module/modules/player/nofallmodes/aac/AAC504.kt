package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object AAC504 : NoFallMode("AAC5.0.4") {
    private var isDamageFalling = false

    override fun onEnable() {
        isDamageFalling = false
    }

    override fun onUpdate() {
        if (mc.thePlayer?.fallDistance ?: 0f > 3f) {
            isDamageFalling = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && isDamageFalling) {
            if (packet.onGround && player.onGround) {
                isDamageFalling = false
                packet.onGround = true
                player.onGround = false
                packet.y += 1.0

                sendPacket(C04PacketPlayerPosition(packet.x, packet.y - 1.0784, packet.z, false), false)
                sendPacket(C04PacketPlayerPosition(packet.x, packet.y - 0.5, packet.z, true), false)
            }
        }
    }
}
