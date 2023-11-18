package net.ccbluex.liquidbounce.features.module.impl.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.impl.combat.criticals.CriticalMode

class MoreCritical : CriticalMode("More") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.00000000001, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}