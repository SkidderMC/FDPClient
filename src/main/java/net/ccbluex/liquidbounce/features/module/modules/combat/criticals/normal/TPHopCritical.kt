package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class TPHopCritical : CriticalMode("TPHop") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.02, ground = false)
        critical.sendCriticalPacket(yOffset = 0.01, ground = false)
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
    }
}