package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class TakaACCritical : CriticalMode("TakaAC") {
    private var attacked = 0

    override fun onAttack(event: AttackEvent) {
        attacked++
        if (attacked >= 5) {
            critical.sendCriticalPacket(yOffset = 0.33319999363422365, ground = false)
            critical.sendCriticalPacket(yOffset = 0.24813599859094576, ground = false)
            critical.sendCriticalPacket(yOffset = 0.16477328182606651, ground = false)
            critical.sendCriticalPacket(yOffset = 0.08307781780646721, ground = false)
            attacked = 0
        }
    }
}
