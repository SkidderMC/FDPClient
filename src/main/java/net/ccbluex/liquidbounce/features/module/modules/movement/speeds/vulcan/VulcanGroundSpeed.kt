package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * VulcanGround Speed
 * Code by Co Dynamic
 * Date: 2023/02/25
 * VulcanStrafe Disabler needed
 */

class VulcanGroundSpeed : SpeedMode("VulcanGround") {
    private val boostSpeedValue = BoolValue("${valuePrefix}GroundBoost", true)
    private val boostDelayValue = IntegerValue("${valuePrefix}BoostDelay", 8, 2, 15).displayable{ boostSpeedValue.get() }
    private val warnings = TextValue("Warning: ", "")
    private val warnings2 = TextValue("Vulcan Strafe", "")
    private val warnings3 = TextValue("Disabler Needed", "")
    private var jumped = false
    private var jumpCount = 0
    private var yMotion = 0.0

    override fun onUpdate() {
        if (jumped) {
            mc.thePlayer.motionY = -0.1
            mc.thePlayer.onGround = false
            jumped = false
            yMotion = 0.0
        }
        mc.thePlayer.jumpMovementFactor = 0.025f
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            if (mc.thePlayer.isCollidedHorizontally || mc.gameSettings.keyBindJump.pressed) {
                if (!mc.gameSettings.keyBindJump.pressed) {
                    mc.thePlayer.jump()
                }
                return
            }
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.0
            yMotion = 0.1 + Math.random() * 0.03
            MovementUtils.strafe(0.48f + jumpCount * 0.001f)
            jumpCount++
            jumped = true
        } else if (MovementUtils.isMoving()) {
            MovementUtils.strafe(0.27f + jumpCount * 0.0018f)
        } else {
            MovementUtils.resetMotion(false)
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        MovementUtils.resetMotion(false)
    }
    
    override fun onEnable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            packet.y += yMotion;
        }
    }

    override fun onMove(event: MoveEvent) {
        if (jumpCount >= boostDelayValue.get() && boostSpeedValue.get()) {
            event.x *= 1.7181145141919810
            event.z *= 1.7181145141919810
            jumpCount = 0
        } else if (!boostSpeedValue.get()) {
            jumpCount = 4
        }
    }
}
