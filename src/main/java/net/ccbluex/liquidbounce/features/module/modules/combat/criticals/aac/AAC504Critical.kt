package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class AAC504Critical : CriticalMode("AAC5.0.4") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.00133545, ground = false)
        critical.sendCriticalPacket(yOffset = -0.000000433, ground = false)
    }
}