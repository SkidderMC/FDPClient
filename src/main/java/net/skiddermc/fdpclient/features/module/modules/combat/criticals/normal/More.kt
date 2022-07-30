package net.skiddermc.fdpclient.features.module.modules.combat.criticals.normal

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class More : CriticalMode("More") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.00000000001, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}