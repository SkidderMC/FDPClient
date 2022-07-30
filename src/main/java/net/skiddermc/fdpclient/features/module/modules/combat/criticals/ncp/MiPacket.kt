package net.skiddermc.fdpclient.features.module.modules.combat.criticals.ncp

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class MiPacket : CriticalMode("MiPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}