package net.skiddermc.fdpclient.utils

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.TickEvent
import net.skiddermc.fdpclient.utils.timer.MSTimer

object PacketCounterUtils : Listenable {

    init {
        FDPClient.eventManager.registerListener(this)
    }

    private var inBound = 0
    private var outBound = 0
    var avgInBound = 0
    var avgOutBound = 0
    private val packetTimer = MSTimer()

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isServerSide()) {
            inBound++
        } else {
            outBound++
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (packetTimer.hasTimePassed(1000L)) {
            avgInBound = inBound
            avgOutBound = outBound
            outBound = 0
            inBound = 0
            packetTimer.reset()
        }
    }

    override fun handleEvents() = true
}