/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import java.awt.Color
import kotlin.math.sqrt

/**
 * Local mirror of the outgoing-movement patterns a server anticheat (GrimAC) keys on, so detection
 * causes can be pinpointed in-game without guessing. Purely diagnostic: it never changes any packet,
 * it only reads what we send and reports the patterns that line up with specific Grim checks.
 *
 * Watched patterns (1.8.9: C03 flying / C04 position / C05 look / C06 position-look are distinct ids):
 *  - AimDuplicateLook: a look packet (C05/C06) whose yaw+pitch equal the previous look's.
 *  - BadPacketsV: a position packet (C04/C06) with near-zero delta sent within 20 ticks of the last.
 *  - GroundSpoof: the packet's onGround flag disagrees with the real player onGround.
 *  - Timer: more than one movement packet emitted inside a single client tick.
 */
object GrimDebug : Module("GrimDebug", Category.CLIENT, gameDetecting = false) {

    private val chatLog by boolean("ChatLog", true)
        .describe("Print a throttled chat warning whenever a Grim-triggering pattern is sent.")
    private val overlay by boolean("Overlay", true)
        .describe("Show a live counter of detected patterns in the top-left corner.")
    private val groundThreshold by float("ZeroDeltaThreshold", 0.03f, 0.001f..0.1f)
        .describe("Position packets moving less than this (blocks) within 20 ticks count as BadPacketsV.")

    private var lastYaw = Float.NaN
    private var lastPitch = Float.NaN
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var hasLastPos = false
    private var ticksSinceLastPos = 99
    private var packetsThisTick = 0

    private var dupLook = 0
    private var zeroDelta = 0
    private var groundSpoof = 0
    private var multiPacket = 0
    private val lastChatAt = LongArray(4)

    override fun onEnable() {
        dupLook = 0; zeroDelta = 0; groundSpoof = 0; multiPacket = 0
        lastYaw = Float.NaN; hasLastPos = false; ticksSinceLastPos = 99; packetsThisTick = 0
    }

    val onTick = handler<GameTickEvent> {
        if (packetsThisTick > 1) {
            multiPacket++
            warn(3, "Timer risk: $packetsThisTick movement packets in one tick")
        }
        packetsThisTick = 0
        ticksSinceLastPos++
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.eventType != EventState.SEND) return@handler
        val packet = event.packet
        if (packet !is C03PacketPlayer) return@handler
        val player = mc.thePlayer ?: return@handler

        packetsThisTick++

        val hasRot = packet is C05PacketPlayerLook || packet is C06PacketPlayerPosLook
        val hasPos = packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook

        if (hasRot) {
            val yaw = packet.yaw
            val pitch = packet.pitch
            if (!lastYaw.isNaN() && yaw == lastYaw && pitch == lastPitch) {
                dupLook++
                warn(0, "AimDuplicateLook: identical look ${fmt(yaw)} / ${fmt(pitch)}")
            }
            lastYaw = yaw
            lastPitch = pitch
        }

        if (hasPos) {
            if (hasLastPos) {
                val dx = packet.x - lastX
                val dy = packet.y - lastY
                val dz = packet.z - lastZ
                val delta = sqrt(dx * dx + dy * dy + dz * dz)
                if (delta < groundThreshold && ticksSinceLastPos < 20) {
                    zeroDelta++
                    warn(1, "BadPacketsV: position delta=${fmt(delta.toFloat())} after $ticksSinceLastPos ticks")
                }
            }
            lastX = packet.x
            lastY = packet.y
            lastZ = packet.z
            hasLastPos = true
            ticksSinceLastPos = 0
        }

        if (packet.onGround != player.onGround) {
            groundSpoof++
            warn(2, "GroundSpoof: claimed ${packet.onGround} but real ${player.onGround}")
        }
    }

    val onRender2D = handler<Render2DEvent> {
        if (!overlay) return@handler
        val font = mc.fontRendererObj ?: return@handler
        val lines = arrayOf(
            "§b[GrimDebug]",
            "§7AimDuplicateLook §f$dupLook",
            "§7BadPacketsV §f$zeroDelta",
            "§7GroundSpoof §f$groundSpoof",
            "§7Timer(multi-pkt) §f$multiPacket"
        )
        var y = 4
        for (line in lines) {
            font.drawStringWithShadow(line, 4f, y.toFloat(), Color.WHITE.rgb)
            y += font.FONT_HEIGHT + 1
        }
    }

    private fun warn(category: Int, message: String) {
        if (!chatLog) return
        val now = System.currentTimeMillis()
        if (now - lastChatAt[category] < 1000L) return
        lastChatAt[category] = now
        chat("§c[GrimDebug] §7$message")
    }

    private fun fmt(value: Float) = String.format("%.2f", value)
}
