/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

object Boost : LongJumpMode("Boost") {
    private var canBoost = false
    private var jumped = false

    override fun onEnable() {
        canBoost = false
        jumped = false
    }

    override fun onUpdate() {
        if (!jumped && mc.thePlayer.onGround) {
            canBoost = false
        }
    }

    override fun onMove(event: MoveEvent) {
        if (jumped) {
            jumped = false
            event.x *= LongJump.boostJumpBoost.toDouble()
            event.z *= LongJump.boostJumpBoost.toDouble()
            return
        }

        if (canBoost) {
            event.x *= LongJump.boostStrafeBoost.toDouble()
            event.z *= LongJump.boostStrafeBoost.toDouble()
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
        jumped = true
        MovementUtils.strafe(LongJump.boostSpeed)
    }
}
