package net.ccbluex.liquidbounce.features.module.modules.movement.flys.rewinside

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

class TeleportRewinsideFly : FlyMode("TeleportRewinside") {
    override fun onUpdate(event: UpdateEvent) {
        val vectorStart = Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
        val yaw = -mc.thePlayer.rotationYaw
        val pitch = -mc.thePlayer.rotationPitch
        val length = 9.9
        val vectorEnd = Vec3(sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.xCoord, sin(Math.toRadians(pitch.toDouble())) * length + vectorStart.yCoord, cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.zCoord)
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vectorEnd.xCoord, mc.thePlayer.posY + 2, vectorEnd.zCoord, true))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vectorStart.xCoord, mc.thePlayer.posY + 2, vectorStart.zCoord, true))
        mc.thePlayer.motionY = 0.0
    }
}