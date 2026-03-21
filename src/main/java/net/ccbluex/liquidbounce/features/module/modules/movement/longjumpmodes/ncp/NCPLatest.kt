/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.ncp

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object NCPLatest : LongJumpMode("NCPLatest") {
    private var canBoost = false
    private var cancelTeleport = false
    private var blinkActive = false

    override fun onEnable() {
        canBoost = false
        cancelTeleport = false
        blinkActive = false

        if (LongJump.ncpLatestWarn) {
            LongJump.sendLegacyWarning("NCPLatest: you must be under a block to bypass.")
        }
    }

    override fun onDisable() {
        if (blinkActive && BlinkUtils.isBlinking) {
            BlinkUtils.unblink()
        }
        blinkActive = false
        canBoost = false
        cancelTeleport = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.onGround) {
            if (canBoost) {
                player.jump()
                if (LongJump.ncpLatestOldMMC) {
                    MovementUtils.strafe(0.7f)
                } else {
                    MovementUtils.strafe(LongJump.ncpLatestBoost)
                }
            }
        } else if (canBoost && !LongJump.ncpLatestOldMMC) {
            MovementUtils.strafe(LongJump.ncpLatestBoost * 0.96f)
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (blinkActive) {
            BlinkUtils.blink(packet, event)
        }

        if (event.eventType == EventState.RECEIVE && packet is S08PacketPlayerPosLook && cancelTeleport) {
            cancelTeleport = false
        }
    }

    override fun onAttemptJump() {
        val player = mc.thePlayer ?: return

        sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false), false)
        sendPacket(
            C04PacketPlayerPosition(
                player.posX,
                player.posY - if (LongJump.ncpLatestOldMMC) 2.0 else 0.1,
                player.posZ,
                false
            ),
            false
        )
        sendPacket(C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false), false)

        if (LongJump.ncpLatestBlink || LongJump.ncpLatestOldMMC) {
            blinkActive = true
        }

        cancelTeleport = true
        canBoost = true
    }

    override fun onAttemptDisable() {
        if (blinkActive && BlinkUtils.isBlinking) {
            BlinkUtils.unblink()
        }
        blinkActive = false
        LongJump.state = false
    }
}
