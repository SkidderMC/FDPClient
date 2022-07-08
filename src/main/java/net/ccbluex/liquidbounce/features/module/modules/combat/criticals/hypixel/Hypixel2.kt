package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.hypixel

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class Hypixel2 : CriticalMode("Hypixel2") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
    }
}