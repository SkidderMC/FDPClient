/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.features.value.BoolValue

class MatrixHop2Speed : SpeedMode("MatrixHop2") {
    private val groundStrafe = BoolValue("${valuePrefix}GroundStrafe", false)

    override fun onUpdate() {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.timer.timerSpeed = 1.0f
                if (groundStrafe.get()) MovementUtils.strafe()
                mc.thePlayer.jump()
            }
            
             if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0012
                mc.thePlayer.motionZ *= 1.0012
                mc.timer.timerSpeed = 1.05f
             }
        }
       
    }
}
