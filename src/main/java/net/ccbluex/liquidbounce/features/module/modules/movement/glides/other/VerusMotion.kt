package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class VerusMotion : GlideMode("VerusMotion") {
	private val speedValue = FloatValue("${valuePrefix}VerusMotionSpeed", 0.3f, 0.1f, 1f)

	override fun onMove(event: MoveEvent) {
		if (MovementUtils.isMoving()) {
			mc.gameSettings.keyBindJump.pressed = false
			if (mc.thePlayer.onGround) {
				MovementUtils.strafe(speedValue.get())
				event.y = 0.41999998688698
			} else {
				MovementUtils.strafe()
			}
		}
	}

	override fun onUpdate() {
		mc.thePlayer.motionY = -0.0784000015258789
	}
}
