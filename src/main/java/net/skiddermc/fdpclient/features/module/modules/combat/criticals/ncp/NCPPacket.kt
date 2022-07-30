package net.skiddermc.fdpclient.features.module.modules.combat.criticals.ncp

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class NCPPacket : CriticalMode("NCPPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.11, ground = false)
        critical.sendCriticalPacket(yOffset = 0.1100013579, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0000013579, ground = false)
    }
}