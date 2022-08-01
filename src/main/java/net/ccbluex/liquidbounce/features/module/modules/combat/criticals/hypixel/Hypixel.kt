package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.hypixel

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class Hypixel : CriticalMode("Hypixel") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.04132332, ground = false)
        critical.sendCriticalPacket(yOffset = 0.023243243674, ground = false)
        critical.sendCriticalPacket(yOffset = 0.01, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0011, ground = false)
    }
}