/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.util.Vec3
import java.math.BigInteger
import java.util.*

object BlinkUtils : MinecraftInstance() {

    val publicPacket: Packet<*>? = null
    val packets = mutableListOf<Packet<*>>()
    val packetsReceived = mutableListOf<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    val positions = mutableListOf<Vec3>()
    val isBlinking
        get() = (packets.size + packetsReceived.size) > 0

    private val playerBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    const val Invalid_Type = -301
    const val MisMatch_Type = -302
    var movingPacketStat = false
    var transactionStat = false
    var keepAliveStat = false
    var actionStat = false
    var abilitiesStat = false
    var invStat = false
    var interactStat = false
    var otherPacket = false

    private var packetToggleStat = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)

    init {
        setBlinkState(off = true, release = true)
        clearPacket()
    }

    fun blink(packet: Packet<*>, event: PacketEvent, sent: Boolean? = true, receive: Boolean? = true) {
        val player = mc.thePlayer ?: return

        if (event.isCancelled || player.isDead) return

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is S02PacketChat, is C01PacketChatMessage -> return
            is S29PacketSoundEffect -> if (packet.soundName == "game.player.hurt") return
        }

        if (mc.currentServerData != null) {
            if (sent == true && receive == false) {
                if (event.eventType == EventState.RECEIVE) {
                    synchronized(packetsReceived) {
                        PacketUtils.queuedPackets.addAll(packetsReceived)
                    }
                    packetsReceived.clear()
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    synchronized(packets) {
                        packets += packet
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) {
                            positions += packetPos
                        }
                    }
                }
            }

            if (receive == true && sent == false) {
                if (event.eventType == EventState.RECEIVE && player.ticksExisted > 10) {
                    event.cancelEvent()
                    synchronized(packetsReceived) {
                        packetsReceived += packet
                    }
                }
                if (event.eventType == EventState.SEND) {
                    synchronized(packets) {
                        sendPackets(*packets.toTypedArray(), triggerEvents = false)
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) {
                            positions += packetPos
                        }
                    }
                    packets.clear()
                }
            }

            if (sent == true && receive == true) {
                // Processa pacotes enviados e recebidos
                if (event.eventType == EventState.RECEIVE && player.ticksExisted > 10) {
                    event.cancelEvent()
                    synchronized(packetsReceived) {
                        packetsReceived += packet
                    }
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    synchronized(packets) {
                        packets += packet
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) {
                            positions += packetPos
                        }
                    }
                }
            }
        }

        if (sent == false && receive == false) unblink()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient == null) {
            clear()
        }
    }

    fun syncSent() {
        synchronized(packetsReceived) {
            PacketUtils.queuedPackets.addAll(packetsReceived)
            packetsReceived.clear()
        }
    }

    fun syncReceived() {
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
            packets.clear()
        }
    }

    fun releasePacket(packetType: String? = null, onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        var count = 0
        when(packetType) {
            null -> {
                count = -1
                for (packets in playerBuffer) {
                    val packetID = BigInteger(packets.javaClass.simpleName.substring(1..2), 16).toInt()
                    if (packetToggleStat[packetID] || !onlySelected) {
                        sendPacket(packets)
                    }
                }
            }
            else -> {
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packets in playerBuffer) {
                    val className = packets.javaClass.simpleName
                    if (className.equals(packetType, ignoreCase = true)) {
                        tempBuffer.add(packets)
                    }
                }
                while(tempBuffer.size > minBuff && (count < amount || amount <= 0)) {
                    sendPacket(tempBuffer.pop())
                    count++
                }
            }
        }
        clearPacket(packetType = packetType, onlySelected = onlySelected, amount = count)
    }

    fun clearPacket(packetType: String? = null, onlySelected: Boolean = false, amount: Int = -1) {
        when(packetType) {
            null -> {
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packets in playerBuffer) {
                    val packetID = BigInteger(packets.javaClass.simpleName.substring(1..2), 16).toInt()
                    if (!packetToggleStat[packetID] && onlySelected) {
                        tempBuffer.add(packets)
                    }
                }
                playerBuffer.clear()
                for (packets in tempBuffer) {
                    playerBuffer.add(packets)
                }
            }
            else -> {
                var count = 0
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packets in playerBuffer) {
                    val className = packets.javaClass.simpleName
                    if (!className.equals(packetType, ignoreCase = true)) {
                        tempBuffer.add(packets)
                    } else {
                        count++
                        if (count > amount) {
                            tempBuffer.add(packets)
                        }
                    }
                }
                playerBuffer.clear()
                for (packets in tempBuffer) {
                    playerBuffer.add(packets)
                }
            }
        }
    }

    fun pushPacket(packets: Packet<*>):Boolean {
        val packetID = BigInteger(packets.javaClass.simpleName.substring(1..2), 16).toInt()
        if (packetToggleStat[packetID] && !isBlacklisted(packets.javaClass.simpleName)) {
            playerBuffer.add(packets as Packet<INetHandlerPlayServer>)
            return true
        }
        return false
    }

    private fun isBlacklisted(packetType: String = ""): Boolean {
        return when(packetType) {
            "C00Handshake", "C00PacketLoginStart", "C00PacketServerQuery", "C01PacketChatMessage", "C01PacketEncryptionResponse", "C01PacketPing" -> true
            else -> false
        }
    }

    fun setBlinkState(
        off: Boolean = false,
        release: Boolean = false,
        all: Boolean = false,
        packetMoving: Boolean = movingPacketStat,
        packetTransaction: Boolean = transactionStat,
        packetKeepAlive: Boolean = keepAliveStat,
        packetAction: Boolean = actionStat,
        packetAbilities: Boolean = abilitiesStat,
        packetInventory: Boolean = invStat,
        packetInteract: Boolean = interactStat,
        other: Boolean = otherPacket
    ) {
        if (release) releasePacket()
        movingPacketStat = (packetMoving && !off) || all
        transactionStat = (packetTransaction && !off) || all
        keepAliveStat = (packetKeepAlive && !off) || all
        actionStat = (packetAction && !off) || all
        abilitiesStat = (packetAbilities && !off) || all
        invStat = (packetInventory && !off )|| all
        interactStat = (packetInteract && !off) || all
        otherPacket = (other && !off) || all
        if (all) {
            for(i in packetToggleStat.indices) packetToggleStat[i] = true
        } else {
            for(i in packetToggleStat.indices) {
                when(i) {
                    0x00 -> packetToggleStat[i] = keepAliveStat
                    0x01, 0x11, 0x12, 0x14, 0x15, 0x17, 0x18, 0x19 -> packetToggleStat[i] = otherPacket
                    0x03, 0x04, 0x05, 0x06 -> packetToggleStat[i] = movingPacketStat
                    0x0F -> packetToggleStat[i] = transactionStat
                    0x02, 0x09, 0x0A, 0x0B -> packetToggleStat[i] = actionStat
                    0x0C, 0x13 -> packetToggleStat[i] = abilitiesStat
                    0x0D, 0x0E, 0x10, 0x16 -> packetToggleStat[i] = invStat
                    0x07, 0x08 -> packetToggleStat[i] = interactStat
                }
            }
        }
    }

    fun unblink() {
        synchronized(packetsReceived) {
            PacketUtils.queuedPackets.addAll(packetsReceived)
        }
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
        }

        clear()

        fakePlayer?.apply {
            mc.theWorld?.removeEntityFromWorld(entityId)
            fakePlayer = null
        }
    }

    fun clear() {
        synchronized(packetsReceived) { packetsReceived.clear() }
        synchronized(packets) { packets.clear() }
        synchronized(positions) { positions.clear() }
    }

    fun addFakePlayer() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        val faker = EntityOtherPlayerMP(world, player.gameProfile)
        faker.rotationYawHead = player.rotationYawHead
        faker.renderYawOffset = player.renderYawOffset
        faker.copyLocationAndAnglesFrom(player)
        faker.rotationYawHead = player.rotationYawHead
        faker.inventory = player.inventory
        world.addEntityToWorld(RandomUtils.nextInt(Int.MIN_VALUE, Int.MAX_VALUE), faker)
        fakePlayer = faker
    }
}
