package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.mineplex

import net.skiddermc.fdpclient.event.*
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class MineplexLongjump : LongJumpMode("Mineplex") {
    private var teleported = false
    override fun onEnable() {
        teleported = false
    }
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.0132099999999999999999999999999
        mc.thePlayer.jumpMovementFactor = 0.08f
        MovementUtils.strafe()
    }

    override fun onJump(event: JumpEvent) {
        event.motion = event.motion * 4.08f
    }
}