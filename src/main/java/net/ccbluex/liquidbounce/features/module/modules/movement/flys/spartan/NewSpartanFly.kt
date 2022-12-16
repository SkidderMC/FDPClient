package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer

class NewSpartanFly : FlyMode("NewSpartan") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0.5f, 8f)

    override fun onEnable() {
        mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ, true))
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = false
        MovementUtils.resetMotion(true)
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            mc.thePlayer.motionY += 1.0
        } else if (mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY -= 1.0
        } else {
            MovementUtils.strafe(speedValue.get())
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            packet.onGround = true
        }
    }
}
