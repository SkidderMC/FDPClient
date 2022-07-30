package net.skiddermc.fdpclient.features.module.modules.combat.criticals.other

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class Mineplex : CriticalMode("Mineplex") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0000000000000045, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}