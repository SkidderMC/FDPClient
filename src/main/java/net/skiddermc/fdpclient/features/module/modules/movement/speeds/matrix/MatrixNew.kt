/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement.speeds.matrix

import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils

class MatrixNew : SpeedMode("MatrixNew") {
    override fun onUpdate() {
        mc.timer.timerSpeed = 1.0f

        if (!MovementUtils.isMoving() || mc.thePlayer.isInWater || mc.thePlayer.isInLava ||
            mc.thePlayer.isOnLadder || mc.thePlayer.isRiding) return

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            mc.timer.timerSpeed = 0.9f
        } else {
            if (mc.thePlayer.fallDistance <= 0.1) {
                mc.timer.timerSpeed = 1.5f
            } else if (mc.thePlayer.fallDistance < 1.3) {
                mc.timer.timerSpeed = 0.7f
            } else {
                mc.timer.timerSpeed = 1.0f
            }
        }
    }

    override fun onMove(event: MoveEvent) {}
}