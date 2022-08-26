/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class GroundPacket : SpeedMode("GroundPacket") {
    
    private val moveSpeed = FloatValue("${valuePrefix}Speed", 0.6f, 0.27f, 5f)
  
    override fun onMove() {
        var s = moveSpeed.get().toDouble()
        var x = mc.thePlayer.posX
        var z = mc.thePlayer.posZ
        var d = 0.2873
        var mx = -sin(yaw) * d
        var mz = cos(yaw) * d
        var yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        while (d < s) {
            if (d > s) {
                d = s
            }
            yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mx = -sin(yaw) * d
            mz = cos(yaw) * d
            PacketUtil.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.thePlayer.setPosition(x + mx, mc.thePlayer.posY, z + mz)
            d += 0.2873
        }
    }
}
