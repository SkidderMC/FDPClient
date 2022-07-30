package net.skiddermc.fdpclient.features.special

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.event.PacketEvent
import net.minecraft.network.handshake.client.C00Handshake

object ServerSpoof : Listenable {
    var enable = false
    var address = "redesky.com"

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (enable && event.packet is C00Handshake) {
            val packet = event.packet
            val ipList = address.split(":").toTypedArray()
            packet.ip = ipList[0]
            if (ipList.size > 1) {
                packet.port = ipList[1].toInt()
            } else {
                packet.port = 25565
            }
        }
    }

    override fun handleEvents() = true
}