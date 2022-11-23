package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class NCPPacketCritical : CriticalMode("NCPPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.11, ground = false)
        critical.sendCriticalPacket(yOffset = 0.1100013579, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0000013579, ground = false)
    }
}