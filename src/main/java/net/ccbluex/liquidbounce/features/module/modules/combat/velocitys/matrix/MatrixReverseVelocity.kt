/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixReverseVelocity : VelocityMode("MatrixReverse") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            mc.thePlayer.motionX = packet.getMotionX().toDouble() / 8000.0
            mc.thePlayer.motionZ = packet.getMotionZ().toDouble() / 8000.0
            MovementUtils.strafe()
        }
    }
}
