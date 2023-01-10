package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin


class MatrixFlagLongjump : LongJumpMode("MatrixFlag") {
  
    var isFlag = false
    var doBoost = false
    override fun onEnable() {
        isFlag = false
        doBoost = false
        sendLegacy()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (isFlag || !doBoost) return
        var yaw = Math.toRadians(MovementUtils.movingYaw.toDouble())
        mc.thePlayer.motionX = -sin(yaw) * 1.89
        mc.thePlayer.motionZ = cos(yaw) * 1.89
        mc.thePlayer.motionY = 0.42
    }
    override fun onPacket(event: PacketEvent) {
        if(event.packet is S08PacketPlayerPosLook) {
            isFlag = true
            return
        }
    }
    override fun onAttemptJump() {
        MovementUtils.strafe()
        doBoost = true
    }
    override fun onJump(event: JumpEvent) {
        MovementUtils.strafe()
        doBoost = true
        event.cancelEvent()
    }
    override fun onAttemptDisable() {
        if (isFlag) {
            longjump.state = false
        }
    }
}
