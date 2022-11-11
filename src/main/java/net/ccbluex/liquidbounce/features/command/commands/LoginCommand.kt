package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command

class LoginCommand : Command("login", arrayOf("l")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        mc.thePlayer.sendChatMessage("/register rrrr rrrr")
        mc.thePlayer.sendChatMessage("/login rrrr")
    }
}