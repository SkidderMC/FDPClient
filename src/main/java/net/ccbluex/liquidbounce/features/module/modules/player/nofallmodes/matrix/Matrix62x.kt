package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object Matrix62x : NoFallMode("Matrix6.2.X") {
    private var matrixCanSpoof = false
    private var matrixFallTicks = 0
    private var matrixIsFall = false
    private var matrixLastMotionY = 0.0

    override fun onEnable() {
        matrixCanSpoof = false
        matrixFallTicks = 0
        matrixIsFall = false
        matrixLastMotionY = 0.0
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (matrixIsFall) {
            player.motionX = 0.0
            player.jumpMovementFactor = 0f
            player.motionZ = 0.0

            if (player.onGround) {
                matrixIsFall = false
            }
        }

        if (player.fallDistance - player.motionY > 3f) {
            matrixIsFall = true

            if (matrixFallTicks == 0) {
                matrixLastMotionY = player.motionY
            }

            player.motionY = 0.0
            player.motionX = 0.0
            player.jumpMovementFactor = 0f
            player.motionZ = 0.0
            player.fallDistance = 3.2f

            if (matrixFallTicks in 8..9) {
                matrixCanSpoof = true
            }

            matrixFallTicks++
        }

        if (matrixFallTicks > 12 && !player.onGround) {
            player.motionY = matrixLastMotionY
            player.fallDistance = 0f
            matrixFallTicks = 0
            matrixCanSpoof = false
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && matrixCanSpoof) {
            packet.onGround = true
            matrixCanSpoof = false
        }
    }
}
