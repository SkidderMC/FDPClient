/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandUtils.player
import net.ccbluex.liquidbounce.features.command.CommandUtils.world
import net.ccbluex.liquidbounce.features.command.TabCompleteUtils

object RemoteViewCommand : Command("remoteview", "rv") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val thePlayer = player() ?: run {
            chat("§cYou must be in a world to use this command.")
            return
        }
        val theWorld = world() ?: run {
            chat("§cYou must be in a world to use this command.")
            return
        }

        if (args.size < 2) {
            if (mc.renderViewEntity != thePlayer) {
                mc.renderViewEntity = thePlayer
                chat("§3Restored your own perspective.")
                return
            }
            chatSyntax("remoteview <username>")
            return
        }

        val targetName = args[1]

        val target = theWorld.loadedEntityList.firstOrNull { targetName == it.name }
        if (target == null) {
            chat("§6Couldn't find anyone named §a$targetName§6 in the world.")
            return
        }

        mc.renderViewEntity = target
        chat("Now viewing perspective of §8${target.name}§3.")
        chat("Execute §8${commandManager.prefix}remoteview §3again to go back to yours.")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        return when (args.size) {
            1 -> TabCompleteUtils.players(args[0])
            else -> emptyList()
        }
    }
}