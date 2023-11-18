package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class TestMinemoraCritical : CriticalMode("TestMinemora") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0114514, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0010999999940395355, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0012016413, ground = false)
    }
}