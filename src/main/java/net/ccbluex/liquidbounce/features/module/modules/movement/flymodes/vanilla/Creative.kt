package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode

object Creative : FlyMode("Creative") {
    override fun onEnable() {
        mc.thePlayer?.capabilities?.isFlying = true
    }

    override fun onUpdate() {
        mc.thePlayer?.capabilities?.isFlying = true
    }
}
