/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import me.zywl.fdpclient.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class PacketCritical : CriticalMode("Packet") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = true)
        critical.sendCriticalPacket(ground = false)
        critical.sendCriticalPacket(yOffset = 1.1E-5, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}