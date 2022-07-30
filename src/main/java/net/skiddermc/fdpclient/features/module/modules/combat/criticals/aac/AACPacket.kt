package net.skiddermc.fdpclient.features.module.modules.combat.criticals.aac

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class AACPacket : CriticalMode("AACPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.01400000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
    }
}