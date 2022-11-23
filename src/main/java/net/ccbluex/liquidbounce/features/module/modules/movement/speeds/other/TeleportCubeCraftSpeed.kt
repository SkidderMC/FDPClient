/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

class TeleportCubeCraftSpeed : SpeedMode("TeleportCubeCraft") {
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