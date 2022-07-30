/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.skiddermc.fdpclient.features.special

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.utils.MinecraftInstance
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.handshake.client.C00Handshake
import java.util.*

class BungeeCordSpoof : MinecraftInstance(), Listenable {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C00Handshake && enabled && packet.requestedState == EnumConnectionState.LOGIN)
            packet.ip = "${packet.ip}\u0000${getRandomIpPart()}.${getRandomIpPart()}.${getRandomIpPart()}.${getRandomIpPart()}\u0000${UUIDSpoofer.getUUID()}"
    }

    private fun getRandomIpPart(): String = RANDOM.nextInt(256).toString()

    override fun handleEvents(): Boolean = true

    companion object {
        private val RANDOM = Random()

        @JvmField
        var enabled = false
    }
}