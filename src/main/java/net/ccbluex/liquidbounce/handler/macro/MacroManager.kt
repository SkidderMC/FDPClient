/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.macro

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.MinecraftInstance

object MacroManager : MinecraftInstance(), Listenable {
    val macros = ArrayList<Macro>()

    @EventTarget
    fun onKey(event: KeyEvent) {
        macros.filter { it.key == event.key }.forEach { it.exec() }
    }

    override fun handleEvents() = true
}