package net.skiddermc.fdpclient.features.module.modules.combat.criticals.hypixel

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode
import net.skiddermc.fdpclient.utils.misc.RandomUtils

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
