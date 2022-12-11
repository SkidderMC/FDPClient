package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.semi

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class VulcanCritical : CriticalMode("Vulcan") {
    private var attacks = 0
    override fun onEnable() {
        attacks = 0
    }
    override fun onAttack(event: AttackEvent) {
        attacks++ //Vulcan updated and it fully bypassed, that's pogggg *bruh
        if (attacks > 6) { //TODO: when you stopped moving it may flags sometimes (doesn't matter)
            critical.sendCriticalPacket(yOffset = 0.2, ground = false)
            critical.sendCriticalPacket(yOffset = 0.1216, ground = false)
            attacks = 0
        }
    }
}
