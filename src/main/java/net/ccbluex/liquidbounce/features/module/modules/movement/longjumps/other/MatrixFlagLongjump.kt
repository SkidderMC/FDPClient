package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class MatrixFlagLongjump : LongJumpMode("MatrixFlag") {
  
    var yes = true
    override fun onEnable() {
        yes = true
    }

    override fun onUpdate(event: UpdateEvent) {
        longjump.no = true //No AutoJump / AutoDisable
        if (!yes) return
        MovementUtils.strafe(1.89f)
        mc.thePlayer.motionY = 0.42
    }
    override fun onPacket(event: PacketEvent) {
        if(event.packet is S08PacketPlayerPosLook) {
            MovementUtils.strafe(1.89f)
            mc.thePlayer.motionY = 0.42
            yes = false
            return
        }
    }
}
