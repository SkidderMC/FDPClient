package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class NCPMotionCritical : CriticalMode("NCPMotion") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.42, ground = false)
        critical.sendCriticalPacket(yOffset = 0.222, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0, ground = true)
    }
}