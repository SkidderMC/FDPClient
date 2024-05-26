/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixSimpleVelocity : VelocityMode("MatrixSimple") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            packet.motionX = (packet.getMotionX() * 0.36).toInt()
            packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
            if (mc.thePlayer.onGround) {
                packet.motionX = (packet.getMotionX() * 0.9).toInt()
                packet.motionZ = (packet.getMotionZ() * 0.9).toInt()
            }
        }
    }
}