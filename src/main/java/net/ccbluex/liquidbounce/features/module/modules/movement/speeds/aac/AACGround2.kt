/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class AACGround2 : SpeedMode("AACGround2") {
    private val timerValue = FloatValue("${valuePrefix}Timer", 3f, 1.1f, 10f)

    override fun onUpdate() {
        if (!MovementUtils.isMoving()) return

        mc.timer.timerSpeed = timerValue.get()

        MovementUtils.strafe(0.02f)
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}