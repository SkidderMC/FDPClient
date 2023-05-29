package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class AttackReduceVelocity : VelocityMode("AttackReduce") {
    val reduceAmount = FloatValue("ReduceAmount", 0.8f, 0.3f, 1f)
    
    override fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime < 3) 
            return
        mc.thePlayer.motionX *= reduceAmount.get().toDouble()
        mc.thePlayer.motionZ *= reduceAmount.get().toDouble()
    }
}
