/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.Target

class TargetCommand : Command("target", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            when {
                args[1].equals("players", ignoreCase = true) -> {
                    Target.playerValue.set(!Target.playerValue.get())
                    alert("§7Target player toggled ${if (Target.playerValue.get()) "on" else "off"}.")
                    playEdit()
                    return
                }

                args[1].equals("mobs", ignoreCase = true) -> {
                    Target.mobValue.set(!Target.mobValue.get())
                    alert("§7Target mobs toggled ${if (Target.mobValue.get()) "on" else "off"}.")
                    playEdit()
                    return
                }

                args[1].equals("animals", ignoreCase = true) -> {
                    Target.animalValue.set(!Target.animalValue.get())
                    alert("§7Target animals toggled ${if (Target.animalValue.get()) "on" else "off"}.")
                    playEdit()
                    return
                }

                args[1].equals("invisible", ignoreCase = true) -> {
                    Target.invisibleValue.set(!Target.invisibleValue.get())
                    alert("§7Target Invisible toggled ${if (Target.invisibleValue.get()) "on" else "off"}.")
                    playEdit()
                    return
                }
            }
        }

        chatSyntax("target <players/mobs/animals/invisible>")
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