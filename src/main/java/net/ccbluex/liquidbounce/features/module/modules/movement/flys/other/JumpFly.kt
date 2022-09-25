package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.FloatValue
import org.lwjgl.input.Keyboard

class JumpFly : FlyMode("Jump") {
    private val motionYValue = FloatValue("${valuePrefix}MotionY", 5f, 0.1f, 5f)

    private var wasDead = false

    override fun onDisable() {
        wasDead = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isDead) {
            wasDead = true
        }

        if (wasDead || mc.thePlayer.onGround) {
            wasDead = false

            mc.thePlayer.jump()
            mc.thePlayer.motionY = motionYValue.get().toDouble()
            mc.thePlayer.onGround = false
        }

        mc.timer.timerSpeed = 1F

    }
}
