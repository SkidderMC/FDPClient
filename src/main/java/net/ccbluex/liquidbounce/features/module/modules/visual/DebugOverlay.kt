/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.PPSCounter
import net.ccbluex.liquidbounce.utils.client.ServerObserver
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.client.Minecraft
import java.awt.Color
import java.util.Locale

/** Compact live diagnostics for movement, rotation, network and server fingerprinting. */
object DebugOverlay : Module("DebugOverlay", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val position by boolean("Position", true)
    private val motion by boolean("Motion", true)
    private val rotation by boolean("Rotation", true)
    private val network by boolean("Network", true)
    private val server by boolean("Server", true)
    private val entities by boolean("Entities", false)
    private val textColor by color("TextColor", Color(235, 240, 255))
    private val x by int("X", 4, 0..1000)
    private val y by int("Y", 4, 0..1000)

    init {
        group("Movement", "Position", "Motion", "Rotation")
        group("Network", "Network", "Server", "Entities")
        group("Appearance", "TextColor", "X", "Y")
    }

    val onRender2D = handler<Render2DEvent> {
        val player = mc.thePlayer
        val lines = buildList {
            add("FPS ${Minecraft.getDebugFPS()}")
            if (player != null && position) add("XYZ ${format(player.posX)} ${format(player.posY)} ${format(player.posZ)}")
            if (player != null && motion) add("Motion ${format(player.motionX)} ${format(player.motionY)} ${format(player.motionZ)}")
            if (rotation) {
                val current = RotationUtils.currentRotation
                val serverRotation = RotationUtils.serverRotation
                add("Rotation ${format(current?.yaw ?: player?.rotationYaw ?: 0f)} ${format(current?.pitch ?: player?.rotationPitch ?: 0f)}")
                add("ServerRot ${format(serverRotation.yaw)} ${format(serverRotation.pitch)}")
            }
            if (network) add("Ping ${ServerObserver.ping}ms | PPS ↑${PPSCounter.getPPS(PPSCounter.PacketType.SEND)} ↓${PPSCounter.getPPS(PPSCounter.PacketType.RECEIVED)}")
            if (server) {
                val tps = ServerObserver.tps.takeIf(Double::isFinite)?.let(::format) ?: "--"
                add("TPS $tps | AC ${ServerObserver.guessAnticheat() ?: "Unknown"}")
                ServerObserver.serverBrand?.let { add("Brand $it") }
            }
            if (entities) add("Entities ${mc.theWorld?.loadedEntityList?.size ?: 0}")
        }

        lines.forEachIndexed { index, line ->
            Fonts.minecraftFont.drawStringWithShadow(line, x.toFloat(), (y + index * 10).toFloat(), textColor.rgb)
        }
    }

    private fun format(value: Number): String = String.format(Locale.ROOT, "%.2f", value.toDouble())
}
