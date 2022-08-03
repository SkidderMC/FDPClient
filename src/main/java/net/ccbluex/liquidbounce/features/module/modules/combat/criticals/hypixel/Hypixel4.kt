package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.hypixel

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

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
