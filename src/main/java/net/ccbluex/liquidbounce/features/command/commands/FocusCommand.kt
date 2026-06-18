/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.TabCompleteUtils
import net.ccbluex.liquidbounce.handler.combat.CombatManager.focusedPlayerList

object FocusCommand : Command("focus") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 3) {
            val focused = args[1]
            val target = args[2]
            val world = mc.theWorld
            if (world == null) {
                chat("§6Couldn't find anyone named §a${target.lowercase()}§6 in the world.")
                return
            }
            val entity = world.playerEntities.filter { it.name.equals(target, true) && !it.equals(mc.thePlayer) }.also {
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

        return when (args.size) {
            1 -> TabCompleteUtils.match(args[0], "clear", "add", "remove")
            2 -> when (args[0].lowercase()) {
                "add" -> TabCompleteUtils.players(args[1], includeSelf = false)
                "remove" -> {
                    val focused = focusedPlayerList.mapNotNull { it.name }
                    TabCompleteUtils.match(args[1], focused)
                }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
