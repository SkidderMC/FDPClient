package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import kotlin.math.cos
import kotlin.math.sin

class CubeCraftFly : FlyMode("CubeCraft") {
    private var timer = TickTimer()

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = 0.6F

        timer.update()
    }

    override fun onMove(event: MoveEvent) {
        val yaw = MovementUtils.direction

        if (timer.hasTimePassed(2)) {
            event.x = -sin(yaw) * 2.4
            event.z = cos(yaw) * 2.4
            timer.reset()
        } else {
            event.x = -sin(yaw) * 0.2
            event.z = cos(yaw) * 0.2
        }
    }
}
