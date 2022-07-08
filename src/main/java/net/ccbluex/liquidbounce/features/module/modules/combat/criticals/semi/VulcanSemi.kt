package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.semi

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class VulcanSemi : CriticalMode("VulcanSemi") {
    private var attacks = 0
    override fun onEnable() {
        attacks = 0
    }
    override fun onAttack(event: AttackEvent) {
        attacks++
        if (attacks > 6) {
            critical.sendCriticalPacket(yOffset = 0.2, ground = false)
            critical.sendCriticalPacket(yOffset = 0.1216, ground = false)
            attacks = 0
        } else {
            critical.antiDesync = false
        }
    }
}