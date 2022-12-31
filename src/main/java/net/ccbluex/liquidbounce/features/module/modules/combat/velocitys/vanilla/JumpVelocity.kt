package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue

class JumpVelocity : VelocityMode("Jump") {
    private val modeValue = ListValue("${valuePrefix}Mode", arrayOf("Motion", "Jump", "Both"), "Jump")
    private val motionValue = FloatValue("${valuePrefix}Motion", 0.42f, 0.4f, 0.5f)
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
            when(modeValue.get().lowercase()) {
                "motion" -> mc.thePlayer.motionY = motionValue.get().toDouble()

                "jump" -> mc.thePlayer.jump()

                "both" -> {
                    mc.thePlayer.motionY = motionValue.get().toDouble()
                    mc.thePlayer.jump()
                }
            }
        }
    }
}
