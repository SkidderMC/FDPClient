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
  
    private var tryFlag = true
    override fun onEnable() {
        tryFlag = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            tryFlag = true
        }
        if (tryFlag) {
            mc.thePlayer.motionY = 0.42
            MovementUtils.strafe(1f)
        } else {
            mc.thePlayer.motionX *= 1.03
            mc.thePlayer.motionZ *= 1.03
        }
    }
    override fun onPacket(event: PacketEvent) {
        if(event.packet is S08PacketPlayerPosLook) {
            tryFlag = false
        }
    }
    override fun onAttemptJump() {
        MovementUtils.strafe()
        tryFlag = true
    }
    override fun onJump(event: JumpEvent) {
        tryFlag = true
        event.cancelEvent()
    }
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
