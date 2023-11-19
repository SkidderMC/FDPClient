package net.ccbluex.liquidbounce.handler.macro

import net.ccbluex.liquidbounce.FDPClient

class Macro(val key: Int, val command: String) {
    fun exec() {
        FDPClient.commandManager.executeCommands(command)
    }
}