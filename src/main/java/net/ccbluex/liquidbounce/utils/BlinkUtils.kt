/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import java.math.BigInteger
import java.util.*

/**
 * BlinkUtils | FDPClient Original
 * Code by Co Dynamic
 * Date: 2023/03/01
 */

//Not finished yet

object BlinkUtils : MinecraftInstance() {
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
        setBlinkState(
            off = true,
            release = true
        )
        clearPacket()
    }

    fun releasePacket(packetType: String? = null, onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        var count = 0
        when(packetType) {
            null -> {
                count = -1
                for (packets in playerBuffer) {
                    val packetID = BigInteger(packets.javaClass.simpleName.substring(1..2), 16).toInt()
                    if (packetToggleStat[packetID] || !onlySelected) {
                        PacketUtils.sendPacketNoEvent(packets)
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
                    PacketUtils.sendPacketNoEvent(tempBuffer.pop())
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

    private fun isBlacklisted(packetType: String = ""):Boolean {
        return when(packetType) {
            "C00Handshake", "C00PacketLoginStart", "C00PacketServerQuery", "C01PacketChatMessage", "C01PacketEncryptionResponse", "C01PacketPing" -> true
            else -> false
        }
    }

    fun setBlinkState() {
        setBlinkState(off = true)
        clearPacket()
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
        if (release) {
            releasePacket()
        }
        movingPacketStat = (packetMoving && !off) || all
        transactionStat = (packetTransaction && !off) || all
        keepAliveStat = (packetKeepAlive && !off) || all
        actionStat = (packetAction && !off) || all
        abilitiesStat = (packetAbilities && !off) || all
        invStat = (packetInventory && !off )|| all
        interactStat = (packetInteract && !off) || all
        otherPacket = (other && !off) || all
        if (all) {
            for(i in packetToggleStat.indices) {
                packetToggleStat[i] = true
            }
        } else {
            for(i in packetToggleStat.indices) {
                when(i) {
                    0x00 -> packetToggleStat[i] = keepAliveStat
                    0x01, 0x11, 0x12, 0x14, 0x15, 0x17, 0x18, 0x19 ->  packetToggleStat[i] = otherPacket
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

    fun bufferSize(packetType: String? = null):Int {
        return when(packetType) {
            null -> playerBuffer.size
            else -> {
                var packetCount = 0
                var flag = false
                for (packets in playerBuffer) {
                    val className = packets.javaClass.simpleName
                    if (className.equals(packetType, ignoreCase = true)) {
                        flag = true
                        packetCount++
                    }
                }
                if (flag) packetCount else MisMatch_Type
            }
        }
    }
}