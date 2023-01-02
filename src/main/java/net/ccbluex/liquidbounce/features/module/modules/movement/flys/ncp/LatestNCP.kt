package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class LatestNCP : FlyMode("LatestNCP") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 7f, 1f, 12f)
    private val timerValue = FloatValue("${valuePrefix}Timer", 0.8F , 0.1f , 1.0f)
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
        if(jumped && !mc.thePlayer.onGround) {
            jumped = false
            MovementUtils.strafe(speedValue.get())
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

}
