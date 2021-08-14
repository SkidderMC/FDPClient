package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.IRC
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class ChatCommand : Command("chat", arrayOf("c")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            if(IRC.state){
                IRC.sendMessage(StringUtils.toCompleteString(args, 1))
                chat("Message was sent to the IRC.")
            }else{
                chat("IRC is not connected. Please toggle the IRC module!")
            }
            return
        }
        chatSyntax("chat <message...>")
    }
}