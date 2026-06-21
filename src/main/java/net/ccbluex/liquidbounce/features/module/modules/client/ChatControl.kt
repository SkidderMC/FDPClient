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
    val chatClearValue by boolean("NoChatClear", true)
    private val fontChat by boolean("FontChat", false)

    private val appendPrefix by boolean("AppendPrefix", false)
    private val prefixText by text("Prefix", "> ") { appendPrefix }
    private val appendSuffix by boolean("AppendSuffix", false)
    private val suffixText by text("Suffix", " | FDP") { appendSuffix }

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