/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.server.S2DPacketOpenWindow
import java.util.*

@ModuleInfo(name = "AntiFrozen",description = "", category = ModuleCategory.OTHER)
class AntiFrozen : Module() {

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S2DPacketOpenWindow && packet.windowTitle.unformattedText.lowercase(Locale.getDefault())
                .contains("frozen")
        )
            event.cancelEvent()
    }
}