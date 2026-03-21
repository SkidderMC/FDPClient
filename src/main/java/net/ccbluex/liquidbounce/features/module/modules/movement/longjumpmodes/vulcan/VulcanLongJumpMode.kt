/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.vulcan

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

object VulcanLongJumpMode : LongJumpMode("Vulcan") {
    private var waitFlag = false
    private var isFlagged = false
    private var lastTickOnGround = false
    private var isDamaged = false
    private var damageJumpCount = 0

    override fun onEnable() {
        val player = mc.thePlayer ?: return

        LongJump.noTimerModify = true
        if (!player.onGround) {
            onAttemptDisable()
            return
        }

        mc.timer.timerSpeed = 1.0f
        waitFlag = false
        isFlagged = false
        isDamaged = false
        damageJumpCount = 0
        lastTickOnGround = player.onGround
        runSelfDamageCore()
    }

    override fun onDisable() {
        LongJump.noTimerModify = false
        MovementUtils.resetMotion(false)
        mc.timer.timerSpeed = 1.0f
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (runSelfDamageCore()) {
            return
        }

        if (LongJump.vulcanOnlyDamage && player.hurtTime == 0 && !waitFlag && !isFlagged && LongJump.airTick < 888) {
            player.onGround = false
            MovementUtils.resetMotion(true)
            player.jumpMovementFactor = 0.0f
            LongJump.airTick = -1
            return
        }

        if (waitFlag && isFlagged) {
            mc.timer.timerSpeed = 0.5f
            MovementUtils.strafe(LongJump.vulcanDistance)

            repeat(LongJump.vulcanRepeatTimes) {
                player.setPosition(player.posX + player.motionX, player.posY, player.posZ + player.motionZ)
                sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true), false)
                sendPacket(C04PacketPlayerPosition(player.posX, player.posY - 0.0784, player.posZ, true), false)
                sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true), false)
            }

            MovementUtils.resetMotion(false)
            waitFlag = false
            isFlagged = false
            LongJump.airTick = 999
        }

        if (waitFlag && !isFlagged && player.onGround) {
            if (!lastTickOnGround) {
                sendPacket(C04PacketPlayerPosition(player.posX, player.posY - 0.0784, player.posZ, true), false)
            }

            mc.timer.timerSpeed = 0.25f
            player.onGround = false
            MovementUtils.resetMotion(true)
            player.jumpMovementFactor = 0.0f
            lastTickOnGround = true
            return
        }

        lastTickOnGround = player.onGround
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (packet) {
            is S08PacketPlayerPosLook -> if (waitFlag) {
                isFlagged = true
            }

            is S12PacketEntityVelocity -> {
                if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                    return
                }

                event.cancelEvent()
            }

            is C03PacketPlayer -> if (damageJumpCount < 4) {
                packet.onGround = false
            }

            is C0FPacketConfirmTransaction -> {
                val transUid = packet.uid.toInt()
                if (transUid in -31767..-30769) {
                    event.cancelEvent()
                    sendPacket(packet, false)
                }
            }
        }
    }

    override fun onAttemptDisable() {
        if (!waitFlag && !isFlagged) {
            LongJump.state = false
        }
    }

    override fun onAttemptJump() {
        val player = mc.thePlayer ?: return

        if (LongJump.vulcanOnlyDamage && player.hurtTime == 0) {
            return
        }

        player.jump()
        MovementUtils.strafe(0.485f)
    }

    override fun onJump(event: JumpEvent) {
        waitFlag = true
        mc.timer.timerSpeed = 1.0f
        lastTickOnGround = true
    }

    private fun runSelfDamageCore(): Boolean {
        val player = mc.thePlayer ?: return false

        if (!LongJump.vulcanOnlyDamage || !LongJump.vulcanSelfDamage) {
            isDamaged = true
            damageJumpCount = 999
            return false
        }

        if (isDamaged) {
            damageJumpCount = 999
            return false
        }

        LongJump.airTick = -1
        player.jumpMovementFactor = 0.0f

        if (player.onGround) {
            if (damageJumpCount >= 4) {
                sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true), false)
                isDamaged = true
                damageJumpCount = 999
                return false
            }

            damageJumpCount++
            MovementUtils.resetMotion(true)
            player.jump()
        }

        MovementUtils.resetMotion(false)
        return true
    }
}
