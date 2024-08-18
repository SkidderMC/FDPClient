/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.macro

import net.ccbluex.liquidbounce.FDPClient.commandManager

class Macro(val key: Int, val command: String) {
    fun exec() {
        commandManager.executeCommands(command)
    }
}