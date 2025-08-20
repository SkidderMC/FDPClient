/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils

object IRCChatCommand : Command("chat", "lc", "irc") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            if (!IRCModule.state) {
                chat("§cError: §7IRC is disabled!")
                return
            }

            val message = StringUtils.toCompleteString(args, 1)

            if (!IRCModule.chatClient.message(message)) {
                chat("§cError: §bIRC is currently not connected to the server!")
                return
            }
        } else
            chatSyntax("chat <message>")
    }
}