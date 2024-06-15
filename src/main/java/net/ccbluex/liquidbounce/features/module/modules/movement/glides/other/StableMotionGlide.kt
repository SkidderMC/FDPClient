package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import me.zywl.fdpclient.value.impl.FloatValue

class StableMotionGlide : GlideMode("StableMotion") {
    private val glideMotionValue = FloatValue("${valuePrefix}Motion", -0.0784f, -1.0f, 0.0f)
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY = glideMotionValue.get().toDouble()
    }
}
