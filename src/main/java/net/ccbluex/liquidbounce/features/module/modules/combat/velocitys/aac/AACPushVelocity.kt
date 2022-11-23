package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue

class AACPushVelocity : VelocityMode("AACPush") {
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F)
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true)
    private var jump = false
    override fun onEnable() {
        jump = false
    }

    override fun onVelocity(event: UpdateEvent) {
        if (jump) {
            if (mc.thePlayer.onGround) {
                jump = false
            }
        } else {
            // Strafe
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0) {
                mc.thePlayer.onGround = true
            }

            // Reduce Y
            if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() &&
                !LiquidBounce.moduleManager[Speed::class.java]!!.state) {
                mc.thePlayer.motionY -= 0.014999993
            }
        }

        // Reduce XZ
        if (mc.thePlayer.hurtResistantTime >= 19) {
            val reduce = aacPushXZReducerValue.get()

            mc.thePlayer.motionX /= reduce
            mc.thePlayer.motionZ /= reduce
        }
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (velocity.onlyGroundValue.get() && !mc.thePlayer.onGround)) {
            return
        }

        if ((velocity.onlyGroundValue.get() && !mc.thePlayer.onGround) || (velocity.onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        jump = true

        if (!mc.thePlayer.isCollidedVertically) {
            event.cancelEvent()
        }
    }
}