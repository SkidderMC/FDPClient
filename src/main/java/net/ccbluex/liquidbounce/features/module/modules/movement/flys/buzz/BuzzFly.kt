/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.buzz

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.sqrt

class BuzzFly : FlyMode("Buzz") {
    private var flag = false
    private var started = false
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0

    override fun onEnable() {
        flag = false
        lastSentX = mc.thePlayer.posX
        lastSentY = mc.thePlayer.posY
        lastSentZ = mc.thePlayer.posZ
        started = false
        MovementUtils.resetMotion(true)
        mc.thePlayer.jumpMovementFactor = 0.00f
        if(mc.thePlayer.onGround) {
            mc.thePlayer.onGround = false
            started = true
            mc.timer.timerSpeed = 0.2f
            PacketUtils.sendPacketNoEvent(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    true
                )
            )
            PacketUtils.sendPacketNoEvent(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY - 2 + Math.random() / 2,
                    mc.thePlayer.posZ,
                    false
                )
            )
            PacketUtils.sendPacketNoEvent(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    true
                )
            )
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (started && flag) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.gameSettings.keyBindSneak.pressed = false
            fly.antiDesync = true
            MovementUtils.strafe((0.96 + Math.random() / 50).toFloat())
            mc.thePlayer.motionY = 0.0
            if(!MovementUtils.isMoving()) {
                MovementUtils.resetMotion(false)
            }
        }else if (started) {
            MovementUtils.resetMotion(true)
            mc.thePlayer.jumpMovementFactor = 0.00f
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        MovementUtils.resetMotion(true)
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is C03PacketPlayer && !flag) {
            event.cancelEvent()
        } else if(packet is C03PacketPlayer && (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook)) {
            val deltaX = packet.x - lastSentX
            val deltaY = packet.y - lastSentY
            val deltaZ = packet.z - lastSentZ
            
            if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > 9.5) {
                lastSentX = packet.x
                lastSentY = packet.y
                lastSentZ = packet.z
                return
            }
            event.cancelEvent()
        }else if(packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if(packet is S08PacketPlayerPosLook) {
            if (!flag) {
                lastSentX = packet.x
                lastSentY = packet.y
                lastSentZ = packet.z
                flag = true
                mc.timer.timerSpeed = 1.0f
                event.cancelEvent()
            }
        }
    }
}
