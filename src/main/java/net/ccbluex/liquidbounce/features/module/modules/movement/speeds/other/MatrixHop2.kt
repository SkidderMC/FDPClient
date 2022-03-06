package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixHop2 : SpeedMode("MatrixHop2") {
    val veloBoostValue = BoolValue("MatrixVelocBoost", true)
	val timerBoostValue = BoolValue("MatrixTimerBoost", true)
    private var recX = 0.0
    private var recZ = 0.0

    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = 0.0266f
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            if (MovementUtils.getSpeed() < 0.217) {
                MovementUtils.strafe(0.217f)
                mc.thePlayer.jumpMovementFactor = 0.0269f
            }
        }
        if (mc.thePlayer.motionY < 0) {
			timer(1.09f)
            if (mc.thePlayer.fallDistance > 1.4)
				timer(1.0f)
        } else {
			timer(0.95f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
			timer(1.03f)
            mc.thePlayer.jump()
            if (mc.thePlayer.movementInput.moveStrafe <= 0.01 && mc.thePlayer.movementInput.moveStrafe >= -0.01) {
                MovementUtils.strafe((MovementUtils.getSpeed() * 1.0071).toFloat())
            }
        } else if (!MovementUtils.isMoving()) {
            timer(1.0f)
        }
        if (MovementUtils.getSpeed() < 0.22)
            MovementUtils.strafe()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

	private fun timer(value: Float) {
		if(timerBoostValue.get()) {
			mc.timer.timerSpeed = value
		}
	}

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!veloBoostValue.get()) return

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            event.cancelEvent()

            recX = packet.motionX / 8000.0
            recZ = packet.motionZ / 8000.0
            if (Math.sqrt(recX * recX + recZ * recZ) > MovementUtils.getSpeed()) {
                MovementUtils.strafe(Math.sqrt(recX * recX + recZ * recZ).toFloat())
                mc.thePlayer.motionY = packet.motionY / 8000.0
            }

            MovementUtils.strafe((MovementUtils.getSpeed() * 1.1).toFloat())
        }
    }
}
