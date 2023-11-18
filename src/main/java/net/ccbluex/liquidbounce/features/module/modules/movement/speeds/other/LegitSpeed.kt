/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue

class LegitSpeed : SpeedMode("Legit") {
    
    private val cpuSPEED = BoolValue("Legit-AlanWOOD-CPU-BYPASS-$$$$", true)
    
    override fun onUpdate() {
        if (cpuSPEED.get()) mc.timer.timerSpeed = 1.004f
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) mc.thePlayer.jump()
        }
    }
}
