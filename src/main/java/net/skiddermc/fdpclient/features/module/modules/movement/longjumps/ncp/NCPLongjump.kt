package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.ncp

import net.skiddermc.fdpclient.event.*
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.FloatValue

class NCPLongjump : LongJumpMode("NCP") {
    private val ncpBoostValue = FloatValue("${valuePrefix}Boost", 4.25f, 1f, 10f)
    private var canBoost = false
    override fun onEnable() {
        canBoost = true
    }
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        MovementUtils.strafe(MovementUtils.getSpeed() * if (canBoost) ncpBoostValue.get() else 1f)
        if(canBoost) canBoost = false
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
    }
}