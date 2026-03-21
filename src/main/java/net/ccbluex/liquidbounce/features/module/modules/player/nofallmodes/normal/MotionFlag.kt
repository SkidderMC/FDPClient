package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.normal

import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.motionFlagSpeed
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode

object MotionFlag : NoFallMode("MotionFlag") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance > 3f) {
            player.motionY = motionFlagSpeed.toDouble()
        }
    }
}
