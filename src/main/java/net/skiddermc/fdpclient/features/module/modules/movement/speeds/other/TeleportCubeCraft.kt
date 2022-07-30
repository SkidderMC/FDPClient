/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement.speeds.other

import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.utils.timer.MSTimer
import net.skiddermc.fdpclient.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

class TeleportCubeCraft : SpeedMode("TeleportCubeCraft") {
    private val portLengthValue = FloatValue("${valuePrefix}PortLength", 1f, 0.1f, 2f)

    private val timer = MSTimer()

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving() && mc.thePlayer.onGround && timer.hasTimePassed(300L)) {
            val yaw = MovementUtils.direction
            event.x = -sin(yaw) * portLengthValue.get()
            event.z = cos(yaw) * portLengthValue.get()
            timer.reset()
        }
    }
}