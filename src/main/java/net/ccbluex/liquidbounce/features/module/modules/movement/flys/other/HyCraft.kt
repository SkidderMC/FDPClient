package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.sqrt

class HyCraft : FlyMode("HyCraft") {
    private val timescale = FloatValue("${valuePrefix}Timer", 1f, 0.1f, 10f)
    private val timer = MSTimer()
    private var flag = false

    override fun onEnable() {
        flag = false
        timer.reset()
        if(mc.thePlayer.onGround) {
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 2 + Math.random() / 2, mc.thePlayer.posZ, false))
        } else {
            fly.state = false
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        fly.antiDesync = false
        MovementUtils.strafe((1.2 + Math.random() / 10).toFloat())
        if(mc.gameSettings.keyBindJump.pressed) {
            mc.thePlayer.motionY = 0.42
        } else if(mc.gameSettings.keyBindSneak.pressed) {
            mc.thePlayer.motionY = 0.42
        } else {
            mc.thePlayer.motionY = 0.0
        }
        if(!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if(timer.hasTimePassed((150 + Math.random() * 50).toLong()) && MovementUtils.isMoving()) {
            timer.reset()
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX , mc.thePlayer.posY , mc.thePlayer.posZ , true))
        }
    }

    override fun onDisable() {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if(packet is S08PacketPlayerPosLook) {
            if (!flag) {
                val deltaX = packet.x - mc.thePlayer.posX
                val deltaY = packet.y - mc.thePlayer.posY
                val deltaZ = packet.z - mc.thePlayer.posZ

                if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) < 10) {
                    event.cancelEvent()
                    PacketUtils.sendPacketNoEvent(
                        C06PacketPlayerPosLook(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.getYaw(),
                            packet.getPitch(),
                            false
                        )
                    )
                }
            }
        }
    }
}
