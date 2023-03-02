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
        if (attacks > 7) { //TODO: when you stopped moving it may flags sometimes (doesn't matter)
            critical.sendCriticalPacket(yOffset = 0.16477328182606651, ground = false)
            critical.sendCriticalPacket(yOffset = 0.08307781780646721, ground = false)
            critical.sendCriticalPacket(yOffset = 0.0030162615090425808, ground = false)
            attacks = 0
        }
    }
}
