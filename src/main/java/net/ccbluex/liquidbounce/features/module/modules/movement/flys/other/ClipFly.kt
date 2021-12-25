package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import kotlin.math.cos
import kotlin.math.sin

class ClipFly : FlyMode("Clip") {
    private val xValue = FloatValue("${valuePrefix}-X", 2f, -5f, 5f)
    private val yValue = FloatValue("${valuePrefix}-Y", 2f, -5f, 5f)
    private val zValue = FloatValue("${valuePrefix}-Z", 2f, -5f, 5f)
    private val delayValue = IntegerValue("${valuePrefix}-Delay", 500, 0, 3000)
    private val motionXValue = FloatValue("${valuePrefix}-MotionX", 0f, -1f, 1f)
    private val motionYValue = FloatValue("${valuePrefix}-MotionY", 0f, -1f, 1f)
    private val motionZValue = FloatValue("${valuePrefix}-MotionZ", 0f, -1f, 1f)

    private val timer = MSTimer()

    override fun onEnable() {
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.onGround = false
        mc.timer.timerSpeed = 0.7f
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        if (timer.hasTimePassed(delayValue.get().toLong())) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.setPosition(mc.thePlayer.posX + (-sin(yaw) * xValue.get()), mc.thePlayer.posY + yValue.get(), mc.thePlayer.posZ + (cos(yaw) * zValue.get()))
            timer.reset()
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
    }
}
