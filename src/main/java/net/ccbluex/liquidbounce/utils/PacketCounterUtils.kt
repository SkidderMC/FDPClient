package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer

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