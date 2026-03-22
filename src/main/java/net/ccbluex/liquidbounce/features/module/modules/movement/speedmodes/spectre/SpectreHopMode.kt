package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.spectre

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

open class SpectreHopMode(
    modeName: String,
    private val jumpMotion: Double,
) : SpeedMode(modeName) {
    override fun onMotion() {
        if (!mc.thePlayer.isMoving || mc.thePlayer.movementInput.jump) {
            return
        }

        if (mc.thePlayer.onGround) {
            strafe(1.1f)
            mc.thePlayer.motionY = jumpMotion
            return
        }

        strafe()
    }
}
