/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import java.text.DecimalFormat

@ModuleInfo(name = "LightningDetect", category = ModuleCategory.WORLD, array = false, defaultOn = true)
class LightningDetect : Module() {

    private val decimalFormat = DecimalFormat("0.0")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S2CPacketSpawnGlobalEntity) {
            val packet = event.packet
            if (packet.func_149053_g() != 1) return
            LiquidBounce.hud.addNotification(Notification(name, "Lightning at X:${decimalFormat.format(packet.func_149051_d() / 32.0)} Y:${decimalFormat.format(packet.func_149050_e() / 32.0)} Z:${decimalFormat.format(packet.func_149049_f() / 32.0)}", NotifyType.WARNING, time = 5000))
        }
    }
}