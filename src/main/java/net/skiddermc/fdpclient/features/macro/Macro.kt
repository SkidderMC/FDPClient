package net.skiddermc.fdpclient.features.macro

import net.skiddermc.fdpclient.FDPClient

class Macro(val key: Int, val command: String) {
    fun exec() {
        FDPClient.commandManager.executeCommands(command)
    }
}