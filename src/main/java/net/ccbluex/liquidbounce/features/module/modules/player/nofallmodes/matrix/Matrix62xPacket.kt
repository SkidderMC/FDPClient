package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.player.NoFall
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object Matrix62xPacket : NoFallMode("Matrix6.2.X-Packet") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!player.onGround && player.fallDistance - player.motionY > 3f) {
            NoFall.wasTimer = true
            mc.timer.timerSpeed = (mc.timer.timerSpeed * if (mc.timer.timerSpeed < 0.6f) 0.25f else 0.5f)
                .coerceAtLeast(0.2f)

            repeat(5) {
                sendPacket(C03PacketPlayer(false), false)
            }

            sendPacket(C03PacketPlayer(true), false)
            sendPacket(C03PacketPlayer(false), false)
            sendPacket(C03PacketPlayer(false), false)

            player.fallDistance = 0f
        }
    }
}
