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
    private var disableLogger = false
    private val packetBuffer = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()

    override fun onEnable() {
        tick = 0
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
    }
    override fun onDisable() {
        tick = 0
        try {
            disableLogger = true
            while (!packetBuffer.isEmpty()) {
                mc.netHandler.addToSendQueue(packetBuffer.take())
            }
            disableLogger = false
        } finally {
            disableLogger = false
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || disableLogger) return
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
    }
    override fun onMotion(event: MotionEvent) {
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
                MovementUtils.strafe(1.7f)
            } else {
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.motionX = 0.0
            }
            if (mc.gameSettings.keyBindJump.pressed) {
                mc.thePlayer.motionY = 1.7
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                mc.thePlayer.motionY = -1.7
            } else {
                mc.thePlayer.motionY = 0.0
            }
        }
    }
}
