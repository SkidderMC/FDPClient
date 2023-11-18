package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class AAC520Velocity : VelocityMode("AAC5.2.0") {
    override fun onVelocityPacket(event: PacketEvent) {
        if(event.packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
        }
    }
}