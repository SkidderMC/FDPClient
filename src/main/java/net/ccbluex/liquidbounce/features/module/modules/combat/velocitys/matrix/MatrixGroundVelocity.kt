package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixGroundVelocity : VelocityMode("MatrixGround") {
    private var isMatrixOnGround = false
    override fun onEnable() {
        isMatrixOnGround = false
    }
    override fun onVelocity(event: UpdateEvent) {
        isMatrixOnGround = mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            packet.motionX = (packet.getMotionX() * 0.36).toInt()
            packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
            if (isMatrixOnGround) {
                packet.motionY = (-628.7).toInt()
                packet.motionX = (packet.getMotionX() * 0.6).toInt()
                packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                mc.thePlayer.onGround = false
            }
        }
    }

}
