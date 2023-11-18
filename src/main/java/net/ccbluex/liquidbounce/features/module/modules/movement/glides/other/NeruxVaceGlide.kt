package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.ccbluex.liquidbounce.features.value.IntegerValue

class NeruxVaceGlide : GlideMode("NeruxVace") {
    private val ticksValue = IntegerValue("${valuePrefix}Ticks", 6, 0, 20)

    private var glideDelay = 0

    override fun onEnable() {
        glideDelay = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            glideDelay++
        }

        if (glideDelay >= ticksValue.get() && !mc.thePlayer.onGround) {
            glideDelay = 0
            mc.thePlayer.motionY = 0.015
        }
    }
}