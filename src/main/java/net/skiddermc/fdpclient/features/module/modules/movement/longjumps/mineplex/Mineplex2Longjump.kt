package net.skiddermc.fdpclient.features.module.modules.movement.longjumps.mineplex

import net.skiddermc.fdpclient.event.*
import net.skiddermc.fdpclient.features.module.modules.movement.longjumps.LongJumpMode
import net.skiddermc.fdpclient.utils.MovementUtils

class Mineplex2Longjump : LongJumpMode("Mineplex2") {
    private var canMineplexBoost = false
    override fun onEnable() {
        canMineplexBoost = false
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!canMineplexBoost) return

        mc.thePlayer.jumpMovementFactor = 0.1f
        if (mc.thePlayer.fallDistance > 1.5f) {
            mc.thePlayer.jumpMovementFactor = 0f
            mc.thePlayer.motionY = (-10f).toDouble()
        }

        MovementUtils.strafe()
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer!!.isCollidedHorizontally) {
            event.motion = 2.31f
            canMineplexBoost = true
            mc.thePlayer!!.onGround = false
        }
    }
}