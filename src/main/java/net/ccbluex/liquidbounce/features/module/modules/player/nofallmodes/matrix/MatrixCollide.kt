package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object MatrixCollide : NoFallMode("MatrixCollide") {
    private var packetCount = 0
    private var needSpoof = false

    override fun onEnable() {
        needSpoof = false
        packetCount = 0
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance.toInt() - player.motionY > 3) {
            player.motionY = 0.0
            player.fallDistance = 0f
            player.motionX *= 0.1
            player.motionZ *= 0.1
            needSpoof = true
        }

        if (player.fallDistance / 3 > packetCount) {
            packetCount = player.fallDistance.toInt() / 3
        }

        if (player.onGround) {
            packetCount = 0
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
