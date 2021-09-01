/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot

class FocusCommand : Command("focus", emptyArray()) {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura
        if (killAura == null) return

        if (args.size == 3) {
            val focused = args[1]
            val target = args[2]

            when (focused.toLowerCase()) {
                "add" -> {
                    killAura.focusEntityName.add(target.toLowerCase())
                    chat("Successfully added §a${target.toLowerCase()}§3 into the focus list.")
                    return
                }
                "remove" -> {
                    if (killAura.focusEntityName.contains(target.toLowerCase())) {
                        killAura.focusEntityName.remove(target.toLowerCase())
                        chat("Successfully removed §a${target.toLowerCase()}§3 from the focus list.")
                        return
                    } else {
                        chat("§6Couldn't find anyone named §a${target.toLowerCase()}§6 in the focus list.")
                        return
                    }
                }
            }
        } else if (args.size == 2 && args[1].equals("clear", true)) {
            killAura.focusEntityName.clear()
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
            2 -> if (args[0].equals("add", true) || args[0].equals("remove", true)) 
                    mc.theWorld.playerEntities
                        .filter { !AntiBot.isBot(it) && it.name.startsWith(pref, true) }
                        .map { it.name }
                        .toList()
                else emptyList()
            else -> emptyList()
        }
    }

}
