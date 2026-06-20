/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.ScreenEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.client.gui.GuiScreenBook
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.network.play.server.S3FPacketCustomPayload

object NoBooks : Module("NoBooks", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val blockWritable by boolean("BlockWritable", false)

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet is S3FPacketCustomPayload && packet.channelName == "MC|BOpen") event.cancelEvent()
    }

    val onScreen = handler<ScreenEvent> { event ->
        if (!blockWritable) return@handler
        val screen = event.guiScreen
        if (screen is GuiScreenBook || screen is GuiEditSign) mc.displayGuiScreen(null)
    }
}