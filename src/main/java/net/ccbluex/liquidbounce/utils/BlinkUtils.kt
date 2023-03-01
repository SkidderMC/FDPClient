package net.ccbluex.liquidbounce.utils

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.PacketUtils



object BlinkUtils : MinecraftInstance() {
    private val playerBuffer = ArrayList<Packet<INetHandlerPlayServer>>()
    private val serverBuffer = ArrayList<Packet<INetHandlerPlayClient?>>()
    private var blinking = false
    private val blinkTimer = MSTimer()

    private var pulseValue = true
    private var pulseDelayValue = 0
    private var playerPositionValue = false
    private var playerAllValue = false
    private var transactionsValue = false
    private var teleportValue = false
    private var velocityValue = false


    fun setBlink(
        pulse: Boolean,
        pulseDelay: Int,
        playerPosition: Boolean,
        playerAll: Boolean,
        transactions: Boolean,
        teleport: Boolean,
        velocity: Boolean
    ) {
        pulseValue = pulse
        pulseDelayValue = pulseDelay
        playerPositionValue = playerPosition
        playerAllValue = playerAll
        transactionsValue = transactions
        teleportValue = teleport
        velocityValue = velocity
    }

    fun setBlink(status: Boolean) {
        blinking = status
        blinkTimer.reset()
    }

    fun clearBuffer(player: Boolean, server: Boolean) {
        blinkTimer.reset()
        if (player)
            playerBuffer.clear()
        if (server)
            serverBuffer.clear()
    }

    fun dispatch() {
        blinkTimer.reset()
        for (packet in playerBuffer) {
            PacketUtils.sendPacketNoEvent(packet)
        }
        for (packet in serverBuffer) {
            PacketUtils.handlePacket(packet)
        }
        clearBuffer(true, true)

    }

    fun onWorld(event: WorldEvent) {
        setBlink(false)
        clearBuffer(true, true)
    }

    fun onPacket(event: PacketEvent) {
        if (!blinking) return
        val packet = event.packet

        if (
            (playerPositionValue && (packet is C03PacketPlayer || packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C05PacketPlayerLook || packet is C03PacketPlayer.C06PacketPlayerPosLook || packet is C0BPacketEntityAction || packet is C0CPacketInput))
            || (playerAllValue && (PacketUtils.getPacketType(packet) == PacketUtils.PacketType.CLIENTSIDE))
            || (transactionsValue && (packet is C00PacketKeepAlive || packet is C0FPacketConfirmTransaction))
            ) {
            event.cancelEvent()
            playerBuffer.add(packet as Packet<INetHandlerPlayServer>)
        }

        if (
            (teleportValue && packet is S08PacketPlayerPosLook)
            || (velocityValue && (packet is S27PacketExplosion || (packet is S12PacketEntityVelocity && (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer))))
            ) {
            event.cancelEvent()
            serverBuffer.add(packet as Packet<INetHandlerPlayClient?>)
        }

        if (pulseValue && blinkTimer.hasTimePassed(pulseDelayValue.toLong())) {
            blinkTimer.reset()
            dispatch()
        }

    }
}

