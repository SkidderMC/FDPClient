package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VerusYPort : SpeedMode("VerusYPort") {
	override fun onMove(event: MoveEvent) {
		if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
			if (MovementUtils.isMoving()) {
				mc.gameSettings.keyBindJump.pressed = false
				if (mc.thePlayer.onGround) {
					mc.thePlayer.jump()
					mc.thePlayer.motionY = 0.0
					MovementUtils.strafe(0.61F)
					event.y = 0.41999998688698
				}
				MovementUtils.strafe()
			}
		}
	}
}
