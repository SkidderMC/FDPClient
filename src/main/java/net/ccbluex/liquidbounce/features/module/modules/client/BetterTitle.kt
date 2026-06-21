/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.network.play.server.S45PacketTitle

object BetterTitle : Module("BetterTitle", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, gameDetecting = false) {

    private val keepTime by boolean("KeepTime", true)
    private val keepTicks by int("KeepTicks", 80, 20..400) { keepTime }
    private val fadeIn by int("FadeIn", 5, 0..40) { keepTime }
    private val fadeOut by int("FadeOut", 10, 0..40) { keepTime }
    private val stripColors by boolean("StripColors", false)

    val onPacket = handler<PacketEvent> { event ->
        if (event.eventType != EventState.RECEIVE) return@handler

        val packet = event.packet as? S45PacketTitle ?: return@handler
        val type = packet.type ?: return@handler

        if (type != S45PacketTitle.Type.TITLE && type != S45PacketTitle.Type.SUBTITLE) return@handler

        val component = packet.message ?: return@handler
        var text = component.formattedText

        if (stripColors) {
            text = stripColor(text)
        }

        val fadeInTicks = if (keepTime) fadeIn else packet.fadeInTime
        val stayTicks = if (keepTime) keepTicks else packet.displayTime
        val fadeOutTicks = if (keepTime) fadeOut else packet.fadeOutTime

        val gui = mc.ingameGUI ?: return@handler

        if (type == S45PacketTitle.Type.TITLE) {
            gui.displayTitle(null, null, fadeInTicks, stayTicks, fadeOutTicks)
            gui.displayTitle(text, null, fadeInTicks, stayTicks, fadeOutTicks)
        } else {
            gui.displayTitle(null, null, fadeInTicks, stayTicks, fadeOutTicks)
            gui.displayTitle(null, text, fadeInTicks, stayTicks, fadeOutTicks)
        }

        event.cancelEvent()
    }
}
