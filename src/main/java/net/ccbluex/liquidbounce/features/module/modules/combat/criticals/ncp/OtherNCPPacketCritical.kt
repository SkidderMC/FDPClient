package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class OtherNCPPacketCritical : CriticalMode("OtherNCPPacket") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.11, ground = true)
        critical.sendCriticalPacket(ground = false)
    }
}
