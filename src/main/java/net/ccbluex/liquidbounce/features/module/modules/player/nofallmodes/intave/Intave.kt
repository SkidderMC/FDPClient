/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.intave

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * Intave no-fall: a pure packet spoof that marks outgoing position packets as grounded once
 * you have fallen far enough to take damage, without touching your motion, which suits
 * Intave's reliance on the on-ground flag rather than vertical-speed prediction.
 */
object Intave : NoFallMode("Intave") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && !player.onGround && player.fallDistance > 3f) {
            packet.onGround = true
        }
    }
}
