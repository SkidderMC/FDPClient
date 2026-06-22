/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.play.client.C03PacketPlayer

/**
 * Spoofs the head rotation sent to the server so other players' aim and backtrack read the
 * wrong direction, while your own view stays untouched. Yaw and pitch are configured
 * separately. Note: because the server sees the faked angles, this works best as an escape/
 * troll tool rather than during your own combat.
 */
object AntiAim : Module("AntiAim", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val yawMode by choices("YawMode", arrayOf("None", "Backwards", "Spin", "Static", "Invert"), "Backwards")
        .describe("How the faked yaw is generated.")
    private val staticYaw by float("StaticYaw", 180f, -180f..180f) { yawMode == "Static" }
        .describe("Fixed yaw angle to send to the server.")
    private val spinSpeed by float("SpinSpeed", 20f, 1f..90f) { yawMode == "Spin" }
        .describe("Degrees the spinning yaw advances per tick.")

    private val pitchMode by choices("PitchMode", arrayOf("None", "Up", "Down", "Static"), "None")
        .describe("How the faked pitch is generated.")
    private val staticPitch by float("StaticPitch", 0f, -90f..90f) { pitchMode == "Static" }
        .describe("Fixed pitch angle to send to the server.")

    private var spin = 0f

    val onPacket = handler<PacketEvent> { event ->
        if (event.eventType != EventState.SEND) {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler
        val packet = event.packet as? C03PacketPlayer ?: return@handler

        when (yawMode) {
            "Backwards" -> packet.yaw = player.rotationYaw + 180f
            "Spin" -> {
                spin = (spin + spinSpeed) % 360f
                packet.yaw = spin
            }
            "Static" -> packet.yaw = staticYaw
            "Invert" -> packet.yaw = -packet.yaw
        }

        when (pitchMode) {
            "Up" -> packet.pitch = -90f
            "Down" -> packet.pitch = 90f
            "Static" -> packet.pitch = staticPitch
        }
    }
}
