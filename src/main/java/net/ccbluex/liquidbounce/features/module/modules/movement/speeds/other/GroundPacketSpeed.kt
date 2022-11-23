/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import kotlin.math.cos
import kotlin.math.sin

class GroundPacketSpeed : SpeedMode("GroundPacket") {
    
    private val moveSpeed = FloatValue("${valuePrefix}Speed", 0.6f, 0.27f, 5f)
    private val baseSpeed = FloatValue("${valuePrefix}DistPerPacket", 0.15f, 0.12f, 0.2873f)
  
    override fun onUpdate() {
        if (!mc.thePlayer.onGround) return
        val s = moveSpeed.get().toDouble()
        var d = baseSpeed.get().toDouble()
        val yaw = Math.toRadians(MovementUtils.movingYaw.toDouble())
        var mx = -sin(yaw) * baseSpeed.get().toDouble()
        var mz = cos(yaw) * baseSpeed.get().toDouble()
        while (d <= s) {
            if (d > s) {
                mx = -sin(yaw) * (d - s)
                mz = cos(yaw) * (d - s)
                d = s
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX + mx, mc.thePlayer.posY, mc.thePlayer.posZ + mz)
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + mx, mc.thePlayer.posY, mc.thePlayer.posZ + mz, mc.thePlayer.onGround))
            d += baseSpeed.get().toDouble()
        }
    }
}
