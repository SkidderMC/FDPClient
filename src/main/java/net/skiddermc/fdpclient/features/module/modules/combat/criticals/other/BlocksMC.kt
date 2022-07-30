package net.skiddermc.fdpclient.features.module.modules.combat.criticals.other

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class BlocksMC : CriticalMode("BlocksMC") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0825080378093, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0215634532004, ground = false)
        critical.sendCriticalPacket(yOffset = 0.1040220332227, ground = false)
    }
}