package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.Listenable

class TipManager : Listenable {
    override fun handleEvents(): Boolean {
        return true
    }
}