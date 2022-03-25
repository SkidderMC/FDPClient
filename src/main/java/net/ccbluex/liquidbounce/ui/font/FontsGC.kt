package net.ccbluex.liquidbounce.ui.font

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.TickEvent

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