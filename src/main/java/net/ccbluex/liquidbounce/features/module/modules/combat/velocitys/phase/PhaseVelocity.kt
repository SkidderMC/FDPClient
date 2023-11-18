package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.phase

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class PhaseVelocity : VelocityMode("Phase") {
    private val phaseHeightValue = FloatValue("${valuePrefix}Height", 0.5F, 0F, 1F)
    private val phaseOnlyGroundValue = BoolValue("${valuePrefix}OnlyGround", true)
    private val phaseMode = ListValue("${valuePrefix}Mode", arrayOf("Normal", "Packet"), "Normal")
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if (!mc.thePlayer.onGround && phaseOnlyGroundValue.get()) {
                return
            }

            when(phaseMode.get().lowercase()) {
                "normal" -> {
                    velocity.velocityInput = true
                    mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ)
                }

                "packet" -> {
                    if (packet.motionX <500 && packet.motionY <500) {
                        return
                    }

                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ, false))
                }
            }
            event.cancelEvent()
            packet.motionX = 0
            packet.motionY = 0
            packet.motionZ = 0
        }
    }
}