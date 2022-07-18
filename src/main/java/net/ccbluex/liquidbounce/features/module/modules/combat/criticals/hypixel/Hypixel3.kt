package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.hypixel

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

class Hypixel3 : CriticalMode("Hypixel3") {
    private var critCounter = 0
    override fun onAttack(event: AttackEvent) {
        critCounter++
        if (critCounter == 1) {
            critical.sendCriticalPacket(yOffset = 0.046875 + RandomUtils.nextInt(0,100) / 10000, ground = false)
        } else if (critCounter == 2) {
            critical.sendCriticalPacket(yOffset = 0.0234375 + RandomUtils.nextInt(0,100) / 10000, ground = false)
        } else {
            critCounter = 0
        }
    }
}
