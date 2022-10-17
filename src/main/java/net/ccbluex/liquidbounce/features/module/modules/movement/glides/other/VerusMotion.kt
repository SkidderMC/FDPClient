package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class VerusMotion : GlideMode("VerusMotion") {
	override fun onUpdate() {
		mc.thePlayer.motionY = -0.0784000015258789
	}
}
