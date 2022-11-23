package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class AAC431OldHYTCritical : CriticalMode("AAC4.3.1OldHYT") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.042487, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0104649713461000007, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0014749900000101, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0000007451816400000, ground = false)
    }
}