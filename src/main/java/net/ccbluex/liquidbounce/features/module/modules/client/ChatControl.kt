/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.network.play.client.C01PacketChatMessage

object ChatControl : Module("ChatControl", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, gameDetecting = false, subjective = true) {

    init {
        state = true
    }

    val chatLimitValue by boolean("NoChatLimit", true)
        .describe("Remove the chat message length limit.")
    val chatClearValue by boolean("NoChatClear", true)
        .describe("Prevent the server from clearing your chat.")
    private val fontChat by boolean("FontChat", false)
        .describe("Render chat using the custom client font.")

    private val appendPrefix by boolean("AppendPrefix", false)
        .describe("Add a prefix to your outgoing messages.")
    private val prefixText by text("Prefix", "> ") { appendPrefix }
        .describe("Text prepended to outgoing messages.")
    private val appendSuffix by boolean("AppendSuffix", false)
        .describe("Add a suffix to your outgoing messages.")
    private val suffixText by text("Suffix", " | FDP") { appendSuffix }
        .describe("Text appended to outgoing messages.")

    fun shouldModifyChatFont() = handleEvents() && fontChat

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet !is C01PacketChatMessage) return@handler

        var message = packet.message
        if (message.startsWith("/")) return@handler

        if (appendPrefix && prefixText.isNotEmpty()) message = prefixText + message
        if (appendSuffix && suffixText.isNotEmpty()) message += suffixText

        packet.message = message
    }
}