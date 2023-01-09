package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class LatestNCP : LongJumpModeMode("LatestNCP") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.65f, 0.28f, 1f)
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.1F , 0.5f , 2.0f)
    private val baseMoveValue = BoolValue("${valuePrefix}MinBaseMoveSpeed", true)
    private val editYmotionValue = BoolValue("${ValuePrefix}EditYMotion", true)
    private var editSpeed = false
    private var editYMotion = false
    private var jumped = false

    override fun onEnable() {
        if(!mc.thePlayer.onGround) {
            fly.state = false
        } else {
            mc.thePlayer.jump()
            jumped = true
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = timerValue.get()
        if (jumped) {
            private var editYMotion = false
            private var jumped = false
        }
        if (mc.thePlayer.motionY < 0.0) {
            editYMotion = true
        }
        if (MovementUtils.getSpeed() < 0.28f) {
            editSpeed = true
        }
        if (editYMotion)
            mc.thePlayer.motionY = -0.0784
        if (editSpeed) 
            MovementUtils.strafe(0.28f)
          
        if(jumped && !mc.thePlayer.onGround) {
            jumped = false
            MovementUtils.strafe(speedValue.get())
        }
        if (mc.thePlayer.onGround) {
          mc.thePlayer.jump()
          jumped = true
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

}
