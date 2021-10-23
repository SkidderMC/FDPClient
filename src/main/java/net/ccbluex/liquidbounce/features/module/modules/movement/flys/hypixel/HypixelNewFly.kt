package net.ccbluex.liquidbounce.features.module.modules.movement.flys.hypixel

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue

class HypixelNewFly : FlyMode("HypixelNew") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.5f, 0.3f, 0.7f)

    private val timer = MSTimer()

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = 0.7f
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        if (timer.hasTimePassed(1000)) {
            // hclip LMFAO
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            val x: Double = -Math.sin(yaw) * speedValue.get()
            val z: Double = Math.cos(yaw) * speedValue.get()
            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
            timer.reset()
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
    }
}