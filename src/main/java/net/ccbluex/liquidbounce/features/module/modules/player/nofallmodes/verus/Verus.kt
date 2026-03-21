package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object Verus : NoFallMode("Verus") {
    private var needSpoof = false

    override fun onEnable() {
        needSpoof = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance - player.motionY > 3f) {
            player.motionY = 0.0
            player.fallDistance = 0f
            player.motionX *= 0.6
            player.motionZ *= 0.6
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
