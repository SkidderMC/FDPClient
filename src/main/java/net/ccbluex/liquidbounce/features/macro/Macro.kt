package net.ccbluex.liquidbounce.features.macro

import net.ccbluex.liquidbounce.LiquidBounce

class Macro(val key: Int, val command: String) {
    fun exec() {
        LiquidBounce.commandManager.executeCommands(command)
    }
}