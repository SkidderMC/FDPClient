package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.*


class MatrixBoost : FlyMode("MatrixBoost") {
    private var boostMotion = 0

    override fun onEnable() {
        boostMotion = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        if (boostMotion == 0) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    true
                )
            )
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX + -sin(yaw) * 1.5,
                    mc.thePlayer.posY + 1,
                    mc.thePlayer.posZ + cos(yaw) * 1.5,
                    false
                )
            )
            boostMotion = 1
            mc.timer.timerSpeed = 0.1f
        } else if (boostMotion == 2) {
            MovementUtils.strafe(1.5f)
            mc.thePlayer.motionY = 0.8
            boostMotion = 3
        } else if (boostMotion < 5) {
            boostMotion++
        } else if (boostMotion >= 5) {
            mc.timer.timerSpeed = 1f
            if (mc.thePlayer.posY < fly.launchY) {
                boostMotion = 0
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(mc.currentScreen == null && packet is S08PacketPlayerPosLook) {
            mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
            mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            if (boostMotion == 1) {
                boostMotion = 2
            }
            event.cancelEvent()
        }
    }
}
