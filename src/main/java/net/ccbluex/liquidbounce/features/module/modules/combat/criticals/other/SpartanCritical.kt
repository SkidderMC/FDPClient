package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class SpartanCritical : CriticalMode("Spartan") {
    private var attacks = 3
    override fun onEnable() {
        attacks = 3
    }
    override fun onAttack(event: AttackEvent) {
        attacks++
        if (attacks >= 3) {
            attacks = 0
            critical.sendCriticalPacket(yOffset = 0.0001, ground = true)
            critical.sendCriticalPacket(ground = false)
        }
    }
}
