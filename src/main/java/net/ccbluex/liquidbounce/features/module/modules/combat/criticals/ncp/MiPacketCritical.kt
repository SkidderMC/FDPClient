package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class MiPacketCritical : CriticalMode("MiPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}