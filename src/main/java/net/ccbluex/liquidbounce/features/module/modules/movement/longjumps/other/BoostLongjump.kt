package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue

class BoostLongjump : LongJumpMode("Boost") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.48f, 0.0f, 3.0f)
    private val jumpBoostValue = FloatValue("${valuePrefix}JumpBoost", 1.5f, 1.0f, 3.0f)
    private val strafeBoostValue = FloatValue("${valuePrefix}StrafeBoost", 1.5f, 1.0f, 3.0f)
    private var canBoost = false
    private var jumped = false
    override fun onEnable() {
        canBoost = false
        jumped = false
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!jumped && mc.thePlayer.onGround) {
            canBoost = false
        }
    }
    override fun onMove(event: MoveEvent) {
        if (jumped) {
            jumped = false
            event.x *= jumpBoostValue.get().toDouble()
            event.z *= jumpBoostValue.get().toDouble()
            return
        }
        if (canBoost) {
            event.x *= strafeBoostValue.get().toDouble()
            event.z *= strafeBoostValue.get().toDouble()
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
        jumped = true
        MovementUtils.strafe(speedValue.get())
    }
    
    override fun onAttemptJump() {
        mc.thePlayer.jump()
    }
    
    override fun onAttemptDisable() {
        canBoost = false
        jumped = false
        longjump.state = false
    }
}
