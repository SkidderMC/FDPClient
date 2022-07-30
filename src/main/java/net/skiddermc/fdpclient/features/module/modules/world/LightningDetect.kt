/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.world

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType
import net.skiddermc.fdpclient.utils.ClientUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import java.text.DecimalFormat

@ModuleInfo(name = "LightningDetect", category = ModuleCategory.WORLD, array = false, defaultOn = true)
class LightningDetect : Module() {
    private val chatlog = BoolValue("debug", false)

    private val decimalFormat = DecimalFormat("0.0")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S2CPacketSpawnGlobalEntity) {
            val packet = event.packet
            if (packet.func_149053_g() != 1) return
            if(chatlog.get()) ClientUtils.displayChatMessage("Lightning at X:${decimalFormat.format(packet.func_149051_d() / 32.0)} Y:${decimalFormat.format(packet.func_149050_e() / 32.0)} Z:${decimalFormat.format(packet.func_149049_f() / 32.0)}")
            FDPClient.hud.addNotification(Notification(name, "Lightning at X:${decimalFormat.format(packet.func_149051_d() / 32.0)} Y:${decimalFormat.format(packet.func_149050_e() / 32.0)} Z:${decimalFormat.format(packet.func_149049_f() / 32.0)}", NotifyType.WARNING, time = 5000))
        }
    }
}
