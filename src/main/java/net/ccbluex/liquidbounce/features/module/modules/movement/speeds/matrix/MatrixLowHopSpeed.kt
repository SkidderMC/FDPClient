package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.event.MoveEvent

class MatrixLowHopSpeed : SpeedMode("MatrixLowHop") {
    
    override fun onUpdate() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            mc.thePlayer.motionY -= 0.116 * 0.03;
        }
    }
}
