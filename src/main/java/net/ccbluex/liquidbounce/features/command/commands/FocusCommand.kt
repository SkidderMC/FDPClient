/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.handler.combat.CombatManager.focusedPlayerList

object FocusCommand : Command("focus") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 3) {
            val focused = args[1]
            val target = args[2]
            val entity = mc.theWorld.playerEntities.filter { it.name.equals(target, true) && !it.equals(mc.thePlayer) }.also {
                if (it.isEmpty()) {
                    chat("§6Couldn't find anyone named §a${target.lowercase()}§6 in the world.")
                    return
                }
            }[0]

            when (focused.lowercase()) {
                "add" -> {
                    focusedPlayerList.add(entity)
                    chat("Successfully added §a${target.lowercase()}§3 into the focus list.")
                    return
                }
                "remove" -> {
                    if (focusedPlayerList.contains(entity)) {
                        focusedPlayerList.remove(entity)
                        chat("Successfully removed §a${target.lowercase()}§3 from the focus list.")
                        return
                    } else {
                        chat("§6Couldn't find anyone named §a${target.lowercase()}§6 in the focus list.")
                        return
                    }
                }
            }
        } else if (args.size == 2 && args[1].equals("clear", true)) {
            focusedPlayerList.clear()
            chat("Successfully cleared the focus list.")
            return
        }

        chatSyntax("focus <clear/add/remove> <target name>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val pref = args[0]

        return when (args.size) {
            1 -> listOf("clear", "add", "remove")
            2 -> if (args[0].equals("add", true) || args[0].equals("remove", true)) {
                    mc.theWorld.playerEntities
                        .filter { it.name.startsWith(pref, true) }
                        .map { it.name }
                        .toList()
            } else emptyList()
            else -> emptyList()
        }
    }
}
