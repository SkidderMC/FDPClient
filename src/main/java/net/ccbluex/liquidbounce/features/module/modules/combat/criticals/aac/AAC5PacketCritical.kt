/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import me.zywl.fdpclient.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class AAC5PacketCritical : CriticalMode("AAC5Packet") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0433, ground = false)
        critical.sendCriticalPacket(yOffset = 0.2088, ground = false)
        critical.sendCriticalPacket(yOffset = 0.9963, ground = false)
    }
}
