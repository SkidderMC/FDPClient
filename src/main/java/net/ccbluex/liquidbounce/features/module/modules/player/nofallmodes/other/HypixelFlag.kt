package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object HypixelFlag : NoFallMode("HypixelFlag") {
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
                event.cancelEvent()

                sendPacket(C04PacketPlayerPosition(packet.x, packet.y, packet.z, false), false)
                sendPacket(C04PacketPlayerPosition(packet.x, packet.y, packet.z - 23.0, true), false)
                sendPacket(C04PacketPlayerPosition(packet.x, packet.y, packet.z, false), false)
            }
        }
    }
}
