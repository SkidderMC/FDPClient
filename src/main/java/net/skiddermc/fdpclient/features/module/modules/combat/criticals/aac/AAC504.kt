package net.skiddermc.fdpclient.features.module.modules.combat.criticals.aac

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class AAC504 : CriticalMode("AAC5.0.4") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.00133545, ground = false)
        critical.sendCriticalPacket(yOffset = -0.000000433, ground = false)
    }
}