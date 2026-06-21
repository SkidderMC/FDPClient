/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.sentinel

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * Sentinel no-fall: cancels the fall at damage range while gently bleeding horizontal speed,
 * then spoofs a single grounded packet so the landing reads as a normal touchdown.
 */
object Sentinel : NoFallMode("Sentinel") {
    private var needSpoof = false

    override fun onEnable() {
        needSpoof = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance - player.motionY > 3f) {
            player.motionY = 0.0
            player.fallDistance = 0f
            player.motionX *= 0.8
            player.motionZ *= 0.8
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
