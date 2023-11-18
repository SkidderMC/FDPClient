package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class BlocksMCCritical : CriticalMode("BlocksMC") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0825080378093, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0215634532004, ground = false)
        critical.sendCriticalPacket(yOffset = 0.1040220332227, ground = false)
    }
}