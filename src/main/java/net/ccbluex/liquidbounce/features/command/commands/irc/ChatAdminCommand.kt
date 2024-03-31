/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands.irc

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule
import java.util.*

class ChatAdminCommand : Command("chatadmin", arrayOf("ircadm", "chatadm")) {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (!IRCModule.state) {
            chat("§cError: §7IRC Module is disabled!")
            return
        }

        if (args.size > 1) {
            when {
                args[1].equals("ban", true) -> {
                    if (args.size > 2) {
                        IRCModule.client.banUser(args[2])
                    } else
                        chatSyntax("chatadmin ban <username>")
                }

                args[1].equals("unban", true) -> {
                    if (args.size > 2) {
                        IRCModule.client.unbanUser(args[2])
                    } else
                        chatSyntax("chatadmin unban <username>")
                }
            }
        } else
            chatSyntax("chatadmin <ban/unban>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty())
            return emptyList()

        return when (args.size) {
            1 -> {
                arrayOf("ban", "unban")
                        .map { it.lowercase(Locale.getDefault()) }
                        .filter { it.startsWith(args[0], true) }
            }
            else -> emptyList()
        }
    }
}