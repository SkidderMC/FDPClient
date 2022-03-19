package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixHop2 : SpeedMode("Matrix6.7.0") {

    override fun onUpdate() {
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            if (MovementUtils.getSpeed() < 0.218) {
                MovementUtils.strafe(0.218f)
            }
        }
        if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
            mc.thePlayer.jumpMovementFactor = 0.02605f
        }else{
            mc.thePlayer.jumpMovementFactor = 0.025f
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
                MovementUtils.strafe((MovementUtils.getSpeed() * 1.003).toFloat())
            }
        }
        if (MovementUtils.getSpeed() < 0.22)
            MovementUtils.strafe()
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            event.cancelEvent()
        }
    }
}
