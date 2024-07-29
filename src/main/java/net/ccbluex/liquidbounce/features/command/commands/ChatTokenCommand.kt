/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.handler.irc.packet.packets.ServerRequestJWTPacket
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object ChatTokenCommand : Command("chattoken") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("chattoken <set/copy/generate>")
            return
        }

        when (args[1].lowercase()) {
            "set" -> {
                if (args.size > 2) {
                    IRCModule.jwtToken = StringUtils.toCompleteString(args, 2)
                    IRCModule.jwt = true

                    if (IRCModule.state) {
                        IRCModule.state = false
                        IRCModule.state = true
                    }
                } else {
                    chatSyntax("chattoken set <token>")
                }
            }

            "generate" -> {
                if (!IRCModule.state) {
                    chat("§cError: §7LiquidChat is disabled!")
                    return
                }

                IRCModule.client.sendPacket(ServerRequestJWTPacket())
            }

            "copy" -> {
                if (IRCModule.jwtToken.isEmpty()) {
                    chat("§cError: §7No token set! Generate one first using '${commandManager.prefix}chattoken generate'.")
                    return
                }

                val stringSelection = StringSelection(IRCModule.jwtToken)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
                chat("§aCopied to clipboard!")
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty())
            return emptyList()

        return when (args.size) {
            1 -> {
                arrayOf("set", "generate", "copy")
                        .map { it.lowercase() }
                        .filter { it.startsWith(args[0], true) }
            }
            else -> emptyList()
        }
    }

}