package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class AAC5PacketCritical : CriticalMode("AAC5Packet") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0625, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0433, ground = false)
        critical.sendCriticalPacket(yOffset = 0.2088, ground = false)
        critical.sendCriticalPacket(yOffset = 0.9963, ground = false)
    }
}
