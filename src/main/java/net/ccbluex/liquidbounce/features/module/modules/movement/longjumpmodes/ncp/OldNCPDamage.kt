/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object OldNCPDamage : LongJumpMode("OldNCPDamage") {
    private val jumpYPosArr = arrayOf(
        0.0, 0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468,
        1.24918707874468, 1.1707870772188, 1.0155550727022, 0.78502770378924, 0.4807108763317, 0.10408037809304
    )

    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var balance = 0
    private var boostSpeed = 1.2f
    private var damageStat = false
    private var hasJumped = false

    override fun onEnable() {
        LongJump.sendLegacyWarning()
        hasJumped = false
        damageStat = false
        balance = if (LongJump.oldNcpDamageInstant) 114514 else 0
        x = mc.thePlayer.posX
        y = mc.thePlayer.posY
        z = mc.thePlayer.posZ
        boostSpeed = LongJump.oldNcpDamageBoostSpeed

        if (!LongJump.oldNcpDamageInstant) {
            chat("LongJump OldNCPDamage: waiting for damage...")
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!damageStat) {
            player.setPosition(x, y, z)
            player.onGround = false
            player.motionY = 0.0
            player.motionX = 0.0
            player.motionZ = 0.0
            player.jumpMovementFactor = 0.0f

            if (balance >= jumpYPosArr.size * 3) {
                repeat(3) {
                    jumpYPosArr.forEach {
                        sendPacket(C04PacketPlayerPosition(x, y + it, z, false), false)
                    }
                }
                sendPacket(C04PacketPlayerPosition(x, y, z, true), false)
                damageStat = true
                player.onGround = true
                LongJump.airTick = 0
            }
        } else if (LongJump.oldNcpDamageMode == "OldHypixel") {
            player.motionY += 0.0049
            if (LongJump.airTick <= 10) {
                MovementUtils.strafe(0.278f * boostSpeed)
                boostSpeed -= 0.0008f + LongJump.oldNcpDamageBoostSpeed * 0.000167f
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        if (LongJump.oldNcpDamageMode == "Normal") {
            MovementUtils.strafe(0.50f * LongJump.oldNcpDamageBoost)
        }
        LongJump.airTick = 0
        hasJumped = true
    }

    override fun onAttemptJump() {
        val player = mc.thePlayer ?: return

        if (damageStat && !hasJumped) {
            player.jump()
        }

        if (LongJump.oldNcpDamageMode == "OldHypixel") {
            MovementUtils.strafe(0.472f + 0.08f * boostSpeed)
            player.motionY = 0.419999
        }
    }

    override fun onAttemptDisable() {
        LongJump.state = false
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && !damageStat) {
            balance++
            event.cancelEvent()
        }
    }
}
