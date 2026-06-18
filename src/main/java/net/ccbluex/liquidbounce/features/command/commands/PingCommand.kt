/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command

object PingCommand : Command("ping") {

    override fun execute(args: Array<String>) {
        val player = mc.thePlayer
        val netHandler = mc.netHandler

        if (player == null || netHandler == null) {
            chat("§cYou need to be in-game to check your ping.")
            return
        }

        val playerInfo = netHandler.getPlayerInfo(player.uniqueID)

        if (playerInfo == null) {
            chat("§cCould not resolve your ping.")
            return
        }

        chat("§3Your ping is §a${playerInfo.responseTime}ms§3.")
    }

}