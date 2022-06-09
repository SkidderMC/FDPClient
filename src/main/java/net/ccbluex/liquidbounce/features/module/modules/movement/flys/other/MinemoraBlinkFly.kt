package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import net.minecraft.client.settings.GameSettings

class MinemoraFly : FlyMode("Minemora") {
    
    private var tick = 0
    private val packetBuffer = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()

    override fun onEnable() {
        tick = 11451 //4
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
    }
    override fun onDisable() {
        tick = 0
        while (!packetBuffer.isEmpty()) {
            mc.netHandler.addToSendQueue(packetBuffer.take())
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }

    override fun onWorld(event: WorldEvent) {
        tick = 0
        while (!packetBuffer.isEmpty()) {
            packetBuffer.take()
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || tick == 0) return
        if (tick == 11451 && packet is C03PacketPlayer) {
            event.cancelEvent()
            return
        }
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook ||
            packet is C08PacketPlayerBlockPlacement ||
            packet is C0APacketAnimation ||
            packet is C0BPacketEntityAction || packet is C02PacketUseEntity) {
            event.cancelEvent()
            packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
        }
    }
    override fun onUpdate(event: UpdateEvent) {
        fly.antiDesync = false
        if (tick == 11451) {
            tick = 0
        }
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        tick++
        mc.timer.timerSpeed = 1.0f
        if (tick == 1) {
            mc.timer.timerSpeed = 0.25f
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.42f, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.thePlayer.jump()
        } else {
            if (MovementUtils.isMoving()) {
                MovementUtils.strafe(((1.7-0.02)/0.98).toFloat()) //no need MotionPre
            } else {
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.motionX = 0.0
            }
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
                mc.thePlayer.motionY = 1.7
            } else if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
                mc.thePlayer.motionY = -1.7
            } else {
                mc.thePlayer.motionY = 0.0
            }
        }
    }
}
