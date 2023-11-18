package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class CancelVelocity : VelocityMode("Cancel") {
    val cancelHorizontal = BoolValue("CancelHorizontalVelocity", true)
    val cancelVertical = BoolValue("CancelVerticalVelocity", true)
    
    override fun onVelocityPacket(event: PacketEvent) {
        event.cancelEvent()
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            if (!cancelVertical.get()) mc.thePlayer.motionY = packet.getMotionY().toDouble() / 8000.0
            if (!cancelHorizontal.get()) {
                mc.thePlayer.motionX = packet.getMotionX().toDouble() / 8000.0
                mc.thePlayer.motionZ = packet.getMotionZ().toDouble() / 8000.0
            }
        }
    }
}
