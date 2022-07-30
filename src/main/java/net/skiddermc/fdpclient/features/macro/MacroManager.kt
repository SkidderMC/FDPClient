package net.skiddermc.fdpclient.features.macro

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.KeyEvent
import net.skiddermc.fdpclient.event.Listenable
import net.skiddermc.fdpclient.utils.MinecraftInstance

class MacroManager : Listenable, MinecraftInstance() {
    val macros = ArrayList<Macro>()

    @EventTarget
    fun onKey(event: KeyEvent) {
        macros.filter { it.key == event.key }.forEach { it.exec() }
    }

    override fun handleEvents() = true
}