package net.skiddermc.fdpclient.features.module.modules.combat.criticals.other

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class TestMinemora : CriticalMode("TestMinemora") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.0114514, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0010999999940395355, ground = false)
        critical.sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0012016413, ground = false)
    }
}