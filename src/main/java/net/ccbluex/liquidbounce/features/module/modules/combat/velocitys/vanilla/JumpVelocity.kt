package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.BoolValue

class JumpVelocity : VelocityMode("Jump") {
    private val modeValue = ListValue("${valuePrefix}Mode", arrayOf("Motion", "Jump", "Both"), "Jump")
    private val motionValue = FloatValue("${valuePrefix}Motion", 0.42f, 0.4f, 0.5f)
    private val failValue = BoolValue("${valuePrefix}SmartFail", true)
    private val failRateValue = FloatValue("${valuePrefix}FailRate", 0.3f, 0.0f, 1.0f).displayable { failValue.get() }
    private val failJumpValue = FloatValue("${valuePrefix}FailJumpRate", 0.25f, 0.0f, 1.0f).displayable { failValue.get() }
    
    private var doJump = true
    private var failJump = false
    private var skipVeloc = false
    
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.onGround && (failJump || mc.thePlayer.hurtTime > 6)) {
            if (failJump) {
                failJump = false
            }
            if (!doJump) {
                skipVeloc = true
            }
            if (Math.random() <= failRateValue.get() && failValue.get()) {
                if (Math.random() <= failJumpValue.get()) {
                    doJump = true
                    failJump = true
                } else {
                    doJump = false
                    failJump = false
                }
            } else {
                doJump = true
                failJump = false
            }
            if (skipVeloc) {
                skipVeloc = false
                return
            }
            when(modeValue.get().lowercase()) {
                "motion" -> mc.thePlayer.motionY = motionValue.get().toDouble()
                "jump" -> mc.thePlayer.jump()
                "both" -> {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = motionValue.get().toDouble()
                }
            }
        }
    }
}
