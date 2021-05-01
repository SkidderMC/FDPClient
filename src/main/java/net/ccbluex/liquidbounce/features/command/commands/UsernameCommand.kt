package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command

class UsernameCommand : Command("username", arrayOf("un")) {
    override fun execute(args: Array<String>) {
        chat("Username: "+mc.thePlayer.name)
        chat("NameTag: "+mc.thePlayer.displayName.formattedText)
    }
}