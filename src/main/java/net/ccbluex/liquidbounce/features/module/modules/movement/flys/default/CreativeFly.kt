package net.ccbluex.liquidbounce.features.module.modules.movement.flys.default

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode

class CreativeFly : FlyMode("Creative") {
    override fun onEnable() {
        mc.thePlayer.capabilities.isFlying = true
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = true
    }

    override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false
    }
}