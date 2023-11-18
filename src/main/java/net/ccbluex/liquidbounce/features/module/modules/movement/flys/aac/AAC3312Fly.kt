package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import org.lwjgl.input.Keyboard

class AAC3312Fly : FlyMode("AAC3.3.12") {
    private val motionValue = FloatValue("${valuePrefix}Motion", 10f, 0.1f, 10f)

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.posY < -70) {
            mc.thePlayer.motionY = motionValue.get().toDouble()
        }

        mc.timer.timerSpeed = 1F

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            mc.timer.timerSpeed = 0.2F
            mc.rightClickDelayTimer = 0
        }
    }
}
