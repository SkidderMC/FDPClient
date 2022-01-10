package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import java.math.BigDecimal
import java.math.RoundingMode

class FreeHypixelFly : FlyMode("FreeHypixel") {
    private val timer = TickTimer()

    override fun onEnable() {
        timer.reset()
        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
    }

    override fun onUpdate(event: UpdateEvent) {
        if (timer.hasTimePassed(10)) {
            mc.thePlayer.capabilities.isFlying = true
            return
        } else {
            mc.thePlayer.rotationYaw = fly.launchYaw
            mc.thePlayer.rotationPitch = fly.launchPitch
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }

        if (fly.launchY == BigDecimal(mc.thePlayer.posY).setScale(3, RoundingMode.HALF_DOWN).toDouble()) {
            timer.update()
        }
    }

    override fun onMove(event: MoveEvent) {
        if (!timer.hasTimePassed(10)) {
            event.zero()
        }
    }
}