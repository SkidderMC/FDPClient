package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class SpoofVelocity : VelocityMode("Spoof") {
    private val modifyTimerValue = BoolValue("ModifyTimer", true)
    private val mtimerValue = FloatValue("Timer", 0.6F, 0.1F, 1F).displayable { modifyTimerValue.get() }
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / 8000.0, mc.thePlayer.posY + packet.motionY / 8000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
            if(modifyTimerValue.get()) {
                mc.timer.timerSpeed = mtimerValue.get()
                velocity.wasTimer = true
            }
        }
    }
}
