package net.skiddermc.fdpclient.features.module.modules.movement.speeds.verus

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.StepEvent
import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.FloatValue

class VerusHop : SpeedMode("VerusHop") {

    private val upTimerValue = BoolValue("${valuePrefix}Up-Timer", true)
    private val upTimerSpeedValue = FloatValue("${valuePrefix}Up-Timer-Speed", 1.27f, 0.1f, 4.0f)
        .displayable { upTimerValue.get() }

    private var wasTimer = false
    private var isStep = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if (MovementUtils.isMoving()) {
            if (isStep) {
                isStep = false
                return
            }

            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()

                if (!mc.thePlayer.isAirBorne) {
                    return //Prevent flag with Fly
                }
                if (upTimerValue.get()) {
                    mc.timer.timerSpeed = upTimerSpeedValue.get()
                }
                wasTimer = true
                MovementUtils.strafe(0.4848f)
            }

            MovementUtils.strafe()
        }
    }

    override fun onEnable() {
        mc.timer.timerSpeed = 1.0f
        isStep = false
        wasTimer = false
    }

    @EventTarget
    fun onStep(e: StepEvent) {
        isStep = true
    }
}
