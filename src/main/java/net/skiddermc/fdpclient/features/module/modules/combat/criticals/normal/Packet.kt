package net.skiddermc.fdpclient.features.module.modules.combat.criticals.normal

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class Packet : CriticalMode("Packet") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = true)
        critical.sendCriticalPacket(ground = false)
        critical.sendCriticalPacket(yOffset = 1.1E-5, ground = false)
        critical.sendCriticalPacket(ground = false)
    }
}