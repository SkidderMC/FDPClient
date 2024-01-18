/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class AACPacketCritical : CriticalMode("AACPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.01400000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
    }
}