package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.potion.Potion

class HYTNewVelocity : VelocityMode("HYTNew") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime in 1..5 && !mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            mc.thePlayer.motionX *= 0.1599999964237213
            mc.thePlayer.motionZ *= 0.1599999964237213
            mc.thePlayer.motionY /= 1.4500000476837158
        }
    }
}