package net.skiddermc.fdpclient.ui.font

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.event.TickEvent

object FontsGC : Listenable {
    private val fontRenderers = mutableListOf<GameFontRenderer>()

    private var gcTicks = 0
    const val GC_TICKS = 200
    const val CACHED_FONT_REMOVAL_TIME = 30000

    @EventTarget
    fun onTick(event: TickEvent) {
        if (gcTicks++ > GC_TICKS) {
            fontRenderers.forEach { it.collectGarbage() }
            gcTicks = 0
        }
    }

    fun register(fontRender: GameFontRenderer) {
        fontRenderers.add(fontRender)
    }

    fun closeAll() {
        fontRenderers.forEach { it.close() }
        fontRenderers.clear()
    }

    override fun handleEvents() = true
}