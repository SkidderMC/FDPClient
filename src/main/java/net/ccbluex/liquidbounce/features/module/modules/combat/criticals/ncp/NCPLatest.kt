package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

// By Koitoyuu - 230303
class NCPLatestCritical : CriticalMode("NCPLatest") {
    private var attacked = 0
    override fun onAttack(event: AttackEvent) {
        attacked ++
        if (attacked >= 5) {
            critical.sendCriticalPacket(yOffset = 0.00001058293536f, ground = false)
            critical.sendCriticalPacket(yOffset = 0.00000916580235f, ground = false)
            critical.sendCriticalPacket(yOffset = 0.00000010371854f, ground = false)
            attacked = 0
        }
    }
}
