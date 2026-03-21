/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.matrix

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.abs

object Matrix663 : NoFallMode("Matrix6.6.3") {
    private var matrixSend = false

    override fun onEnable() {
        matrixSend = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        val collLoc = FallingPlayer(player).findCollision(60)

        if (player.fallDistance - player.motionY > 3 ||
            (abs((collLoc?.pos?.y ?: 0) - player.posY) < 3 && player.fallDistance - player.motionY > 2)
        ) {
            player.fallDistance = 0f
            matrixSend = true

            if (NoFall.matrixSafe) {
                mc.timer.timerSpeed = 0.3f
                player.motionX *= 0.5
                player.motionZ *= 0.5
            } else {
                mc.timer.timerSpeed = 0.5f
            }
        } else {
            mc.timer.timerSpeed = 1f
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (event.eventType != EventState.SEND || !matrixSend) {
            return
        }

        val packet = event.packet as? C03PacketPlayer ?: return
        val player = mc.thePlayer ?: return

        matrixSend = false

        val collLoc = FallingPlayer(player).findCollision(60)
        if (abs((collLoc?.pos?.y ?: 0) - player.posY) > 2) {
            event.cancelEvent()
            sendPacket(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
            sendPacket(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, false))
        }
    }
}
