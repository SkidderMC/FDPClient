/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.animalValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.invisibleValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.mobValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.playerValue

object TargetCommand : Command("target") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("target <players/mobs/animals/invisible>")
        }

        when (args[1].lowercase()) {
            "players" -> {
                playerValue = !playerValue
                chat("§7Target player toggled ${if (playerValue) "on" else "off"}.")
                playEdit()
            }

            "mobs" -> {
                mobValue = !mobValue
                chat("§7Target mobs toggled ${if (mobValue) "on" else "off"}.")
                playEdit()
            }

            "animals" -> {
                animalValue = !animalValue
                chat("§7Target animals toggled ${if (animalValue) "on" else "off"}.")
                playEdit()
            }

            "invisible" -> {
                invisibleValue = !invisibleValue
                chat("§7Target Invisible toggled ${if (invisibleValue) "on" else "off"}.")
                playEdit()
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("players", "mobs", "animals", "invisible")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}