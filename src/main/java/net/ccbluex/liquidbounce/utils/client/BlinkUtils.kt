/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S06PacketUpdateHealth
import net.minecraft.network.play.server.S07PacketRespawn
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.util.Vec3

object BlinkUtils : MinecraftInstance, Listenable {

    private const val MAX_QUEUED_PACKETS = 8192

    val packets = mutableListOf<Packet<*>>()
    val packetsReceived = mutableListOf<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    val positions = mutableListOf<Vec3>()
    val isBlinking
        get() = (packets.size + packetsReceived.size) > 0

    fun blink(packet: Packet<*>, event: PacketEvent, sent: Boolean? = true, receive: Boolean? = true) {
        val player = mc.thePlayer ?: return

        if (event.isCancelled || player.isDead || mc.currentServerData == null) return

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is S02PacketChat, is C01PacketChatMessage -> {
                return
            }

            is S29PacketSoundEffect -> {
                if (packet.soundName == "game.player.hurt") {
                    return
                }
            }

            // Corrections and lifecycle packets must never wait behind the blink queue. Flush the
            // matching backlog first so the current packet is processed in the correct order.
            is S08PacketPlayerPosLook,
            is S40PacketDisconnect,
            is S07PacketRespawn,
            is S01PacketJoinGame -> {
                unblink(immediateIncoming = true)
                return
            }

            is S06PacketUpdateHealth -> {
                if (packet.health <= 0f) {
                    unblink(immediateIncoming = true)
                    return
                }
            }
        }

        if (queuedPacketCount() >= MAX_QUEUED_PACKETS) {
            ClientUtils.LOGGER.warn("[BlinkUtils] Packet queue reached $MAX_QUEUED_PACKETS entries; flushing")
            unblink()
            return
        }

        if (sent == true && receive == false) {
            if (event.eventType == EventState.RECEIVE) {
                synchronized(packetsReceived) {
                    PacketUtils.schedulePacketProcess(packetsReceived)
                }
                packetsReceived.clear()
            }
            if (event.eventType == EventState.SEND) {
                event.cancelEvent()
                synchronized(packets) {
                    packets += packet
                }
                if (packet is C03PacketPlayer && packet.isMoving) {
                    synchronized(positions) {
                        positions += packet.pos
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
                    synchronized(positions) {
                        positions += packet.pos
                    }
                }
                packets.clear()
            }
        }

        if (sent == true && receive == true) {
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
                    synchronized(positions) {
                        positions += packet.pos
                    }
                }
            }
        }

        if (sent == false && receive == false)
            unblink()
    }

    @Suppress("unused")
    private val onWorld = handler<WorldEvent> { event ->
        // Clear packets on disconnect only
        if (event.worldClient == null) {
            clear()
            removeFakePlayer()
        }
    }

    fun syncSent() {
        synchronized(packetsReceived) {
            PacketUtils.schedulePacketProcess(packetsReceived)
            packetsReceived.clear()
        }
    }

    fun syncReceived() {
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
            packets.clear()
        }
    }

    fun cancel() {
        val firstPosition = synchronized(positions) { positions.firstOrNull() }
        val player = mc.thePlayer
        if (player != null && firstPosition != null) {
            player.setPositionAndUpdate(firstPosition.xCoord, firstPosition.yCoord, firstPosition.zCoord)
        }

        val copy = synchronized(packets) {
            packets.toTypedArray().also { packets.clear() }
        }

        for (packet in copy) {
            if (packet !is C03PacketPlayer) {
                sendPacket(packet, triggerEvent = false)
            }
        }

        synchronized(packetsReceived) { packetsReceived.clear() }
        synchronized(positions) { positions.clear() }
        removeFakePlayer()
    }

    fun unblink(immediateIncoming: Boolean = false) {
        val incoming = synchronized(packetsReceived) { packetsReceived.toTypedArray() }
        if (immediateIncoming) {
            PacketUtils.handlePackets(incoming.asList())
        } else {
            PacketUtils.schedulePacketProcess(incoming.asList())
        }
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
        }

        clear()

        removeFakePlayer()
    }

    fun clear() {
        synchronized(packetsReceived) {
            packetsReceived.clear()
        }

        synchronized(packets) {
            packets.clear()
        }

        synchronized(positions) {
            positions.clear()
        }
    }

    fun addFakePlayer() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val faker = EntityOtherPlayerMP(world, player.gameProfile).apply {
            copyLocationAndAnglesFrom(player)
            rotationYaw = player.rotationYaw
            rotationPitch = player.rotationPitch
            rotationYawHead = player.rotationYawHead
            renderYawOffset = player.renderYawOffset
            inventory = player.inventory
        }

        removeFakePlayer()
        world.addEntityToWorld(RandomUtils.nextInt(Int.MIN_VALUE, Int.MAX_VALUE), faker)

        fakePlayer = faker

        // Add positions indicating a blink start
        // val pos = thePlayer.positionVector
        // positions += pos.addVector(.0, thePlayer.eyeHeight / 2.0, .0)
        // positions += pos
    }

    private fun queuedPacketCount(): Int =
        synchronized(packets) { packets.size } + synchronized(packetsReceived) { packetsReceived.size }

    private fun removeFakePlayer() {
        val entity = fakePlayer ?: return
        mc.theWorld?.removeEntityFromWorld(entity.entityId)
        fakePlayer = null
    }
}
