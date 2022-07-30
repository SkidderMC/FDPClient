package net.skiddermc.fdpclient.features.module.modules.combat.criticals.ncp

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class NCPMotion : CriticalMode("NCPMotion") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.42, ground = false)
        critical.sendCriticalPacket(yOffset = 0.222, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0, ground = true)
    }
}