package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer

object OldVulcan : NoFallMode("OldVulcan") {
    private var vulcanNoFall = false
    private var vulcanCantNoFall = false
    private var nextSpoof = false
    private var doSpoof = false

    override fun onEnable() {
        vulcanNoFall = false
        vulcanCantNoFall = false
        nextSpoof = false
        doSpoof = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!vulcanNoFall && player.fallDistance > 3.25f) {
            vulcanNoFall = true
        }

        if (vulcanNoFall && player.onGround && vulcanCantNoFall) {
            vulcanCantNoFall = false
        }

        if (vulcanCantNoFall) {
            return
        }

        if (nextSpoof) {
            player.motionY = -0.1
            player.fallDistance = -0.1f
            MovementUtils.strafe(0.3f)
            nextSpoof = false
        }

        if (player.fallDistance > 3.5625f) {
            player.fallDistance = 0f
            doSpoof = true
            nextSpoof = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && doSpoof) {
            packet.onGround = true
            doSpoof = false
            packet.y = kotlin.math.round(player.posY * 2.0) / 2.0
            player.setPosition(player.posX, packet.y, player.posZ)
        }
    }
}
