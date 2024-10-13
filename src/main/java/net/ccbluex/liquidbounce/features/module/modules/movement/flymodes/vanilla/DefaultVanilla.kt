/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.handleVanillaKickBypass
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.keepAliveValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.kickBypassModeValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.kickBypassMotionSpeedValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.kickBypassValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.noClipValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.smoothValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.speedValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.spoofValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.vspeedValue
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.resetMotion
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer

object DefaultVanilla : FlyMode("DefaultVanilla") {

    private var packets = 0
    private var kickBypassMotion = 0f

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer

    override fun onEnable() {
        packets = 0
        kickBypassMotion = 0f
    }

    fun onUpdate(event: UpdateEvent) {
        if (keepAliveValue) {
            mc.netHandler.addToSendQueue(C00PacketKeepAlive())
        }

        if (noClipValue) {
            player.noClip = true
        }

        if(kickBypassValue) {
            if(kickBypassModeValue === "Motion") {
                kickBypassMotion = kickBypassMotionSpeedValue

                if (player.ticksExisted % 2 == 0) {
                    kickBypassMotion = -kickBypassMotion
                }

                if(!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    player.motionY = kickBypassMotion.toDouble()
                }
            }
        }

        if(smoothValue) {
            player.capabilities.isFlying = true
            player.capabilities.flySpeed = speedValue * 0.05f
        } else {
            player.capabilities.isFlying = false
            resetMotion(true)
            if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += vspeedValue

            if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vspeedValue

            MovementUtils.strafe(speedValue)
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if(spoofValue) packet.onGround = true
            if (packets++ >= 40 && kickBypassValue) {
                packets = 0
                if(kickBypassModeValue === "Packet") {
                    handleVanillaKickBypass()
                }
            }
        }
    }
}
