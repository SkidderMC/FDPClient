package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue

class AAC305Fly : FlyMode("AAC3.0.5") {
    private val fastValue = BoolValue("${valuePrefix}Fast", true)

    private var delay = 0

    override fun onUpdate(event: UpdateEvent) {

        if (delay == 2) {
            mc.thePlayer.motionY = 0.1
        } else if (delay > 2) {
            delay = 0
        }

        if (fastValue.get()) {
            if (mc.thePlayer.movementInput.moveStrafe.toDouble() == 0.0) mc.thePlayer.jumpMovementFactor =
                0.08f else mc.thePlayer.jumpMovementFactor = 0f
        }

        delay++
    }
}