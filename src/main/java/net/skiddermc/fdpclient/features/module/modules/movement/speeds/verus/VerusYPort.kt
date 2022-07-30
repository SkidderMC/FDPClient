package net.skiddermc.fdpclient.features.module.modules.movement.speeds.verus

import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.modules.movement.speeds.SpeedMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.FloatValue

class VerusYPort : SpeedMode("VerusYPort") {
	private val speedValue = FloatValue("${valuePrefix}Speed", 0.61f, 0.1f, 1f)

	override fun onMove(event: MoveEvent) {
		if (MovementUtils.isMoving()) {
			mc.gameSettings.keyBindJump.pressed = false
			if (mc.thePlayer.onGround) {
				mc.thePlayer.jump() // sproof jump
				mc.thePlayer.motionY = 0.0
				MovementUtils.strafe(speedValue.get())
				event.y = 0.41999998688698
			} else {
				MovementUtils.strafe()
			}
		}
	}
}
