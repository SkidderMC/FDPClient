package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.client.settings.GameSettings
import kotlin.math.sqrt

class GodseyeFly : FlyMode("Godseye") {
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
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        fly.antiDesync = true
        MovementUtils.strafe((0.26 + Math.random() / 10).toFloat())
        if(mc.gameSettings.keyBindJump.pressed)/*if(GameSettings.isKeyDown(mc.gameSettings.keyBindJump))*/ {
            mc.thePlayer.motionY = 0.42
        } else if(mc.gameSettings.keyBindSneak.pressed)/* if(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)  */ {
            mc.thePlayer.motionY = 0.42
        } else {
            mc.thePlayer.motionY = 0.0
        }
        if(!MovementUtils.isMoving()) {
            MovementUtils.resetMotion(false)
        }
        if(timer.hasTimePassed((150 + Math.random() * 50).toLong()) && MovementUtils.isMoving()) {
            timer.reset()
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX , mc.thePlayer.posY , mc.thePlayer.posZ , true))
        }
    }

    override fun onDisable() {
        MovementUtils.resetMotion(true)
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
