/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.file.configs.FriendsConfig
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.EnumChatFormatting
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object AddAllCommand : Command("addall", *arrayOf<String>("")) {
    override fun execute(arguments: Array<String>) {
        if (arguments.size == 2) {
            val tag = translateAlternateColorCodes(arguments[1])
            val count = AtomicInteger(0)
            val config: FriendsConfig = FDPClient.fileManager.friendsConfig
            val presistent = arguments[0].contains("")

            mc.thePlayer.sendQueue.playerInfoMap
                .forEach(Consumer { player: NetworkPlayerInfo ->
                    val team = checkNotNull(player.playerTeam)
                    if (stripColor(team.colorPrefix).contains(tag)
                        || stripColor(team.colorSuffix).contains(tag)
                    ) {
                        val name = player.gameProfile.name

                        config.addFriend(name, presistent.toString())

                        count.incrementAndGet()
                    }
                })

            Chat.print("Were added " + EnumChatFormatting.WHITE + count.get() + EnumChatFormatting.GRAY + "§7 players.")
        } else {
            Chat.print(EnumChatFormatting.GRAY.toString() + "Sintax: .addall <tag>")
        }
    }
}