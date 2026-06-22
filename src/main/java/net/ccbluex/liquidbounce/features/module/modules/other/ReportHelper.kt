/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.async.launchSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.minecraft.network.play.server.S02PacketChat

object ReportHelper : Module("ReportHelper", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val delayTicks by intRange("Delay", 1..3, 0..20, "ticks")
        .describe("Random tick delay before sending the report.")
    private val chance by int("Chance", 100, 1..100, "%")
        .describe("Percent chance to report a matched player.")
    private val pattern by text("CommandPattern", "/report %s")
        .describe("Command template where %s is the player name.")
    private val ignoreFriends by boolean("IgnoreFriends", true)
        .describe("Never report players on your friends list.")
    private val onlyOnline by boolean("OnlyOnlinePlayers", true)
        .describe("Only report players present in the tab list.")

    private val reported = sortedSetOf(String.CASE_INSENSITIVE_ORDER)

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet !is S02PacketChat) {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler
        val message = packet.chatComponent?.unformattedText?.trim() ?: return@handler
        if (message.isEmpty()) {
            return@handler
        }

        val selfName = player.name
        if (!message.contains(selfName)) {
            return@handler
        }

        val candidates = if (onlyOnline) {
            mc.netHandler?.playerInfoMap?.mapNotNull { it.gameProfile?.name } ?: return@handler
        } else {
            mc.theWorld?.playerEntities?.mapNotNull { it.name } ?: return@handler
        }

        val target = candidates.firstOrNull { name ->
            name != selfName &&
                message.contains(name) &&
                !(ignoreFriends && friendsConfig.isFriend(name))
        } ?: return@handler

        if (nextInt(0, 100) >= chance || !reported.add(target)) {
            return@handler
        }

        launchSequence {
            delay(delayTicks.random() * 50L)
            mc.thePlayer?.sendChatMessage(pattern.format(target))
        }
    }

    val onWorld = handler<WorldEvent> {
        reported.clear()
    }

    override fun onDisable() {
        reported.clear()
        super.onDisable()
    }

}
