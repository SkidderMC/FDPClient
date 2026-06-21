/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.polar

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * Polar no-fall: cancels the fall a little before damage range and flags the very next
 * position packet as grounded, keeping the spoof to a single tick the way Polar expects.
 */
object Polar : NoFallMode("Polar") {
    private var needSpoof = false

    override fun onEnable() {
        needSpoof = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance - player.motionY > 2.5f) {
            player.motionY = 0.0
            player.fallDistance = 0f
            needSpoof = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && needSpoof) {
            packet.onGround = true
            needSpoof = false
        }
    }
}
