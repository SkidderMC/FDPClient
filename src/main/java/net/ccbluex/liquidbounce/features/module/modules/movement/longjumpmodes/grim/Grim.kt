/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.grim

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

/**
 * Conservative long jump for Grim: a single launch boost on the jump tick plus a light
 * sustained strafe multiplier while airborne, kept modest so the horizontal speed stays
 * inside Grim's prediction envelope.
 */
object Grim : LongJumpMode("Grim") {
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
            event.x *= 1.3
            event.z *= 1.3
            return
        }

        if (canBoost) {
            event.x *= 1.25
            event.z *= 1.25
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
        jumped = true
        MovementUtils.strafe(0.4f)
    }
}
