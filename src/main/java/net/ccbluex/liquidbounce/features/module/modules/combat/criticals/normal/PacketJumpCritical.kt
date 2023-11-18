package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class PacketJumpCritical : CriticalMode("PacketJump") {
    override fun onAttack(event: AttackEvent)  {
        critical.sendCriticalPacket(yOffset = 0.41999998688698, ground = false)
        critical.sendCriticalPacket(yOffset = 0.7531999805212, ground = false)
        critical.sendCriticalPacket(yOffset = 1.00133597911214, ground = false)
        critical.sendCriticalPacket(yOffset = 1.16610926093821, ground = false)
        critical.sendCriticalPacket(yOffset = 1.24918707874468, ground = false)
        critical.sendCriticalPacket(yOffset = 1.1707870772188, ground = false)
        critical.sendCriticalPacket(yOffset = 1.0155550727022, ground = false)
        critical.sendCriticalPacket(yOffset = 0.78502770378924, ground = false)
        critical.sendCriticalPacket(yOffset = 0.4807108763317, ground = false)
        critical.sendCriticalPacket(yOffset = 0.10408037809304, ground = false)
        critical.sendCriticalPacket(yOffset = 0.0, ground = true)
    }
}
