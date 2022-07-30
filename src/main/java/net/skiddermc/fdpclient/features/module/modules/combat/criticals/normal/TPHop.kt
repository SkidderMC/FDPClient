package net.skiddermc.fdpclient.features.module.modules.combat.criticals.normal

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode

class TPHop : CriticalMode("TPHop") {
    override fun onAttack(event: AttackEvent) {
        critical.sendCriticalPacket(yOffset = 0.02, ground = false)
        critical.sendCriticalPacket(yOffset = 0.01, ground = false)
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
    }
}