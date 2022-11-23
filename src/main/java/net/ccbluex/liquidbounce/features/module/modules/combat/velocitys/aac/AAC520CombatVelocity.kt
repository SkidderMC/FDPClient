package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class AAC520CombatVelocity : VelocityMode("AAC5.2.0Combat") {
    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0
    override fun onEnable() {
        templateX = 0
        templateY = 0
        templateZ = 0
    }

    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime> 0 && velocity.velocityInput) {
            velocity.velocityInput = false
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.jumpMovementFactor = -0.002f
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
        }
        if (velocity.velocityTimer.hasTimePassed(80L) && velocity.velocityInput) {
            velocity.velocityInput = false
            mc.thePlayer.motionX = templateX / 8000.0
            mc.thePlayer.motionZ = templateZ / 8000.0
            mc.thePlayer.motionY = templateY / 8000.0
            mc.thePlayer.jumpMovementFactor = -0.002f
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            velocity.velocityInput = true
            templateX = packet.motionX
            templateZ = packet.motionZ
            templateY = packet.motionY
        }
    }
}