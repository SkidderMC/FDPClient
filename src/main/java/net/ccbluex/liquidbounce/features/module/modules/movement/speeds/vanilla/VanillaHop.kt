package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vanilla

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class VanillaHop : SpeedMode("VanillaHop") {
    private val vanillaspeed = FloatValue("VanillaHop-Speed", 1F, 0.1F, 9.5F)

    override fun onUpdate() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            MovementUtils.strafe(vanillaspeed.get())
        }
        MovementUtils.strafe()
    }

}