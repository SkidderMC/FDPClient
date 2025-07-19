/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.exploit.PacketDebugger.packetType
import net.ccbluex.liquidbounce.features.module.modules.exploit.PacketDebugger.selectedPackets
import net.ccbluex.liquidbounce.utils.client.ClientUtils

object PacketDebuggerCommand : Command("packetdebugger", "debug") {

    private val packetList: Set<String> = runCatching {
        javaClass.getResourceAsStream("/assets/minecraft/fdpclient/packets.txt")!!.bufferedReader().readLines().toSet()
    }.onFailure {
        ClientUtils.LOGGER.error("Failed to load packet list", it)
    }.getOrDefault(emptySet())

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (packetType != "Custom") {
            chat("§cPlease select 'Custom' mode in PacketDebugger first.")
            return
        }

        val usedAlias = args[0].lowercase()

        if (args.size < 2) {
            chatSyntax("$usedAlias <add/remove/list>")
            return
        }

        when (args[1].lowercase()) {
            "add" -> {
                if (args.size < 3) {
                    chatSyntax("$usedAlias add <packet>")
                    return
                }

                val packetName = args[2]
                if (selectedPackets.contains(packetName)) {
                    chat("§cPacket §b$packetName §cis already in the debug list.")
                } else {
                    selectedPackets.add(packetName)
                    chat("§b$packetName §ahas been added to the list.")
                }
            }
            "remove" -> {
                if (args.size < 3) {
                    chatSyntax("$usedAlias remove <packet>")
                    return
                }

                val packetName = args[2]
                if (selectedPackets.contains(packetName)) {
                    selectedPackets.remove(packetName)
                    chat("§b$packetName §6has been removed from the list.")
                } else {
                    chat("§b$packetName §cis not in the list.")
                }
            }
            "list" -> {
                val packets = selectedPackets
                chat("Packets List:")
                packets.forEach { chat("§b$it") }
            }
            else -> chatSyntax("$usedAlias <add/remove/list>")
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("add", "remove", "list").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "add" -> {
                        val input = args[1].lowercase()
                        packetList.filter { it.lowercase().startsWith(input) }
                    }
                    "remove" -> {
                        val input = args[1].lowercase()
                        selectedPackets.filter { it.lowercase().startsWith(input) }
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

}