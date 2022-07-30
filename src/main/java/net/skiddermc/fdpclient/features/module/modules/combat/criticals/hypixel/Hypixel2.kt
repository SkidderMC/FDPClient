package net.skiddermc.fdpclient.features.module.modules.combat.criticals.hypixel

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class Hypixel2 : CriticalMode("Hypixel2") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
    }
}