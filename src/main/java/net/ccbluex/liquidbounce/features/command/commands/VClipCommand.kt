/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandUtils.doubleArg
import net.ccbluex.liquidbounce.features.command.CommandUtils.player

object VClipCommand : Command("vclip", "verticalclip") {

    override fun execute(args: Array<String>) {
        val amount = args.doubleArg(1)
        val player = player()
        if (amount == null || player == null) {
            chatSyntax("vclip <finite distance>")
            return
        }

        player.setPositionAndUpdate(player.posX, player.posY + amount, player.posZ)
        chat("Vertically clipped §a$amount§3 blocks.")
    }
}
