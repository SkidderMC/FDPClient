/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.PacketUtils
import java.util.*

/**
 * BlinkUtils
 * Code by Co Dynamic
 * Date: 2023/03/01
 */

//Not finished yet

object BlinkUtils : MinecraftInstance() {
    private val playerBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    const val Invalid_Type = -301
    const val MisMatch_Type = -302
    init {
        clearPacket()
    }

    fun releasePacket(packetType: String? = null) {
        when(packetType) {
            null -> {
                for (packets in playerBuffer) {
                    PacketUtils.sendPacketNoEvent(packets)
                }
            }
            else -> {
                for (packets in playerBuffer) {
                    val className = packets.javaClass.simpleName
                    if (className.equals(packetType, ignoreCase = true)) {
                        PacketUtils.sendPacketNoEvent(packets)
                    }
                }
            }
        }
        clearPacket(packetType)
    }

    fun clearPacket(packetType: String? = null) {
        when(packetType) {
            null -> {
                playerBuffer.clear()
            }
            else -> {
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packets in playerBuffer) {
                    val className = packets.javaClass.simpleName
                    if (!className.equals(packetType, ignoreCase = true)) {
                        tempBuffer.add(packets)
                    }
                }
                playerBuffer.clear()
                for (packets in tempBuffer) {
                    playerBuffer.add(packets)
                }
            }
        }
    }

    fun pushPacket(packets: Packet<INetHandlerPlayServer>) {
        playerBuffer.add(packets)
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