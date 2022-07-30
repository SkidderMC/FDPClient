package net.skiddermc.fdpclient.features.module.modules.movement.flys.other

import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.utils.timer.TickTimer
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
