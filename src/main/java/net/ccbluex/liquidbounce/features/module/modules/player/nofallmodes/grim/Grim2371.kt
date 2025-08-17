/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.grim

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * Bypassing GrimAC Anti Cheat (Adapted from nextgen)
 * Minecraft Version 1.9+
 *
 * @author XeContrast (original), adapted for FDPClient
 */
object Grim2371 : NoFallMode("Grim2371") {

    private val timer = TickTimer()
    private var shouldCancel = false
    private var waitingForGround = false

    override fun onEnable() {
        timer.reset()
        shouldCancel = false
        waitingForGround = false
    }

    override fun onDisable() {
        shouldCancel = false
        waitingForGround = false
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (player.onGround || player.fallDistance < 2.5) {
            waitingForGround = false
            return
        }

        if (!waitingForGround && player.fallDistance >= 2.5) {
            waitingForGround = true
            shouldCancel = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && shouldCancel && waitingForGround) {
            if (player.onGround) {
                // Send ground packet to bypass GrimAC
                event.cancelEvent()
                sendPacket(C03PacketPlayer.C04PacketPlayerPosition(
                    player.posX, player.posY, player.posZ, true
                ))
                shouldCancel = false
                waitingForGround = false
            }
        }
    }
}