/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.EnumChatFormatting
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.script.api.global.Chat
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object AddAllCommand : Command("addall", arrayOf("").toString()) {
    override fun execute(arguments: Array<String>) {
        if (arguments.size == 2) {
            val tag = translateAlternateColorCodes(arguments[1])
            val count = AtomicInteger(0)
            val friendManager = friendsConfig
            val presistent = arguments[0].contains("")

            mc.thePlayer.sendQueue.playerInfoMap.forEach(
                Consumer { player: NetworkPlayerInfo ->
                    val team = player.playerTeam
                    if (team != null) {
                        if (ColorUtils.stripColor(team.colorPrefix).contains(tag) ||
                            ColorUtils.stripColor(team.colorSuffix).contains(tag)
                        ) {
                            val name = player.gameProfile.name
                            if (!friendManager.isFriend(name)) {
                                friendManager.addFriend(name)
                                count.incrementAndGet()
                                Chat.print("§b[§b!§b]§b ADDED: $name")
                            }
                        }
                    }
                }
            )

            Chat.print("Were added " + EnumChatFormatting.DARK_RED + count.get() + EnumChatFormatting.GRAY + " players.")
        } else {
            chat("§bUse: addall <tag>")
        }
    }
}