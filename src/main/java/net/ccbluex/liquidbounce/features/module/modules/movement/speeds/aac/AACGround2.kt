/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AACGround2 : SpeedMode("AACGround2") {
    override fun onUpdate() {
        if (!MovementUtils.isMoving()) return

        mc.timer.timerSpeed =
            (LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed?)!!.aacGroundTimerValue.get()

        MovementUtils.strafe(0.02f)
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}