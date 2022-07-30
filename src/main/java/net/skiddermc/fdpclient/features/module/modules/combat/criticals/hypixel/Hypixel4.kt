package net.skiddermc.fdpclient.features.module.modules.combat.criticals.hypixel

import net.skiddermc.fdpclient.event.AttackEvent
import net.skiddermc.fdpclient.features.module.modules.combat.criticals.CriticalMode
import net.skiddermc.fdpclient.utils.misc.RandomUtils

class Hypixel4 : CriticalMode("Hypixel4") {
    private var critValue = true
  
    override fun onAttack(event: AttackEvent) {
        critValue = !critValue
      
        if (critValue) {
            critical.sendCriticalPacket(yOffset = 0.01, ground = false)
        } else {
            critical.sendCriticalPacket(yOffset = 0.007, ground = false)    
        }
    }
}
