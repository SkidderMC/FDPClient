package net.skiddermc.fdpclient.features.module.modules.combat.criticals.other

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class VerusSmart : CriticalMode("VerusSmart") {
    private var attacks = 0
    override fun onEnable() {
        attacks = 0
    }
    override fun onAttack(event: AttackEvent) {
        attacks++
        if (attacks > 4) {
            attacks = 0

            critical.sendCriticalPacket(yOffset = 0.001, ground = true)
            critical.sendCriticalPacket(ground = false)
        } else {
            critical.antiDesync = false
        }
    }
}