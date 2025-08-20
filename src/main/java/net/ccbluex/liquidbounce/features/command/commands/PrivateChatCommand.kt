/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils

object PrivateChatCommand : Command("pchat", "privatechat", "lcpm") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            if (!IRCModule.state) {
                chat("§cError: §7IRC is disabled!")
                return
            }

            val target = args[1]
            val message = StringUtils.toCompleteString(args, 2)

            if (!IRCModule.chatClient.privateMessage(target, message)) {
                chat("§cError: §7IRC is currently not connected to the server!")
                return
            }

            chat("Message was successfully sent.")
        } else
            chatSyntax("pchat <username> <message>")
    }
}