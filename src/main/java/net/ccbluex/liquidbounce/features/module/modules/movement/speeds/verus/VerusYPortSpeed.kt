package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue

class VerusYPortSpeed : SpeedMode("VerusYPort") {
	private val speedValue = FloatValue("${valuePrefix}Speed", 0.61f, 0.1f, 1f)

	override fun onMove(event: MoveEvent) {
		if (MovementUtils.isMoving()) {
			mc.gameSettings.keyBindJump.pressed = false
			if (mc.thePlayer.onGround) {
				mc.thePlayer.jump()
				mc.thePlayer.motionY = 0.0
				MovementUtils.strafe(speedValue.get())
				event.y = 0.41999998688698
			} else {
				MovementUtils.strafe()
			}
		}
	}
}
