/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import kotlin.math.cos
import kotlin.math.sin

class NCPPacketFly : FlyMode("NCPPacket") {
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.1f, 1.0f, 1.3f)
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.28f, 0.27f, 0.29f)

    override fun onUpdate(event: UpdateEvent) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val x = -sin(yaw) * speedValue.get()
        val z = cos(yaw) * speedValue.get()
        MovementUtils.resetMotion(true)
        mc.timer.timerSpeed = timerValue.get()
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY , mc.thePlayer.motionZ + z, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY - 490, mc.thePlayer.motionZ + z, true))
        mc.thePlayer.posX += x
        mc.thePlayer.posZ += z
    }
}
