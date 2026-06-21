/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.hypixel

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

/**
 * Long jump aimed at Hypixel/Watchdog: a stronger launch impulse with a firmer airborne
 * strafe multiplier for noticeably longer jumps where Watchdog tolerates higher speed.
 */
object Hypixel : LongJumpMode("Hypixel") {
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
            event.x *= 1.45
            event.z *= 1.45
            return
        }

        if (canBoost) {
            event.x *= 1.3
            event.z *= 1.3
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
        jumped = true
        MovementUtils.strafe(0.45f)
    }
}
