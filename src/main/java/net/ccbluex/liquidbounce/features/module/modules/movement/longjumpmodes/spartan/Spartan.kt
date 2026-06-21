/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.spartan

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

/**
 * Balanced long jump for Spartan: a moderate launch boost and airborne strafe multiplier,
 * tuned between the conservative Grim profile and the stronger Hypixel one.
 */
object Spartan : LongJumpMode("Spartan") {
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
            event.x *= 1.35
            event.z *= 1.35
            return
        }

        if (canBoost) {
            event.x *= 1.28
            event.z *= 1.28
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
        jumped = true
        MovementUtils.strafe(0.42f)
    }
}
