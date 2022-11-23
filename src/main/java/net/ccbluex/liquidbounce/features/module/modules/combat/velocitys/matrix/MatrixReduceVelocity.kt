package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class MatrixReduceVelocity : VelocityMode("MatrixReduce") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            if (mc.thePlayer.onGround) {
                if (mc.thePlayer.hurtTime <= 6) {
                    mc.thePlayer.motionX *= 0.70
                    mc.thePlayer.motionZ *= 0.70
                }
                if (mc.thePlayer.hurtTime <= 5) {
                    mc.thePlayer.motionX *= 0.80
                    mc.thePlayer.motionZ *= 0.80
                }
            } else if (mc.thePlayer.hurtTime <= 10) {
                mc.thePlayer.motionX *= 0.60
                mc.thePlayer.motionZ *= 0.60
            }
        }
    }
}