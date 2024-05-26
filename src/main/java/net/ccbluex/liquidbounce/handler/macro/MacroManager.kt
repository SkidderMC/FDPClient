/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.macro

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.KeyEvent
import me.zywl.fdpclient.event.Listenable
import net.ccbluex.liquidbounce.utils.MinecraftInstance

class MacroManager : Listenable, MinecraftInstance() {
    val macros = ArrayList<Macro>()

    @EventTarget
    fun onKey(event: KeyEvent) {
        macros.filter { it.key == event.key }.forEach { it.exec() }
    }

    override fun handleEvents() = true
}