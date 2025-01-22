/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.macro

import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

object MacroManager : MinecraftInstance, Listenable {
    val macros = ArrayList<Macro>()

    val onKey = handler<KeyEvent> { event ->
        macros.filter { it.key == event.key }.forEach { it.exec() }
    }

    override fun handleEvents() = true
}