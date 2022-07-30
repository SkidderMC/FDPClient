package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.move.flys.FlyMode
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import kotlin.math.cos
import kotlin.math.sin

class NCPPacketFly : FlyMode("NCPPacket") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.287f, 0.0f, 0.3f)
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.1f, 1.0f, 1.5f)

    override fun onUpdate(event: UpdateEvent) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val x = -sin(yaw) * speedValue.get()
        val z = cos(yaw) * speedValue.get()
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.timer.timerSpeed = timerValue.get()
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY - 490, mc.thePlayer.posZ + z, true))
    }
}
