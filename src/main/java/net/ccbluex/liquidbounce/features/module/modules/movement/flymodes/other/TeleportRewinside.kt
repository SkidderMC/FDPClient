package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

object TeleportRewinside : FlyMode("TeleportRewinside") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        val vectorStart = Vec3(player.posX, player.posY, player.posZ)
        val yaw = -player.rotationYaw
        val pitch = -player.rotationPitch
        val length = 9.9
        val vectorEnd = Vec3(
            sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.xCoord,
            sin(Math.toRadians(pitch.toDouble())) * length + vectorStart.yCoord,
            cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.zCoord
        )

        sendPacket(C04PacketPlayerPosition(vectorEnd.xCoord, player.posY + 2, vectorEnd.zCoord, true), false)
        sendPacket(C04PacketPlayerPosition(vectorStart.xCoord, player.posY + 2, vectorStart.zCoord, true), false)

        player.motionY = 0.0
    }
}
