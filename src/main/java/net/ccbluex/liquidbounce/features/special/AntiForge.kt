/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.PacketBuffer
import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.event.EventTarget
import java.lang.Exception

object AntiForge : MinecraftInstance(), Listenable {
    var enabled = true
    var blockFML = true
    var blockProxyPacket = true
    var blockPayloadPackets = true

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (enabled && !mc.isIntegratedServerRunning) {
            try {
                if (blockProxyPacket && packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket")
                    event.cancelEvent()

                if (blockPayloadPackets && packet is C17PacketCustomPayload) {
                    if (!packet.channelName.startsWith("MC|")){
                        event.cancelEvent()
                    } else if (packet.channelName.equals("MC|Brand", true)) {
                        packet.data = PacketBuffer(Unpooled.buffer()).writeString("vanilla")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun handleEvents() = true
}