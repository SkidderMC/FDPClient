/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.play.server.S01PacketJoinGame
import net.ccbluex.liquidbounce.utils.client.ServerUtils.remoteIp
import net.ccbluex.liquidbounce.utils.client.ServerObserver
import java.util.Locale

object AnticheatDetector : Module("AntiCheatDetector", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private const val WARMUP_TICKS = 60

    private val debug by boolean("Debug", false)
        .describe("Print transaction action numbers to chat for debugging.")
    private var check = false
    private var ticksPassed = 0
    private var loggedOnce = false

    var detectedACName: String = ""
        private set

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is S01PacketJoinGame) {
            reset()
            check = true
        }
    }

    val onTick = handler<GameTickEvent> {
        if (!check) return@handler
        if (ticksPassed++ < WARMUP_TICKS) return@handler

        val result = ServerObserver.guessAnticheat(remoteIp) ?: "Unknown"
        if (result != detectedACName) {
            detectedACName = result
            if (result != "Unknown") notify(result)
        }
        if (debug && result == "Unknown" && !loggedOnce) {
            loggedOnce = true
            logNumbers()
        }
    }

    private fun notify(message: String) {
        val brand = ServerObserver.serverBrand ?: "?"
        val tps = ServerObserver.tps
        val tpsText = if (tps.isFinite()) String.format(Locale.ROOT, "%.1f", tps) else "?"
        val ping = ServerObserver.ping
        hud.addNotification(
            Notification(
                "Alert",
                "§3AC: $message §7| Brand $brand | TPS $tpsText | ${ping}ms",
                Type.WARNING,
                3000
            )
        )
    }

    private fun logNumbers() {
        val actions = ServerObserver.initialTransactions
        chat("Action Numbers: ${actions.joinToString()}")
        chat("Differences: ${actions.windowed(2) { it[1] - it[0] }.joinToString()}")
    }

    private fun reset() {
        ticksPassed = 0
        check = false
        loggedOnce = false
        detectedACName = ""
    }

    override fun onEnable() = reset()

    init {
        state = true
    }
}
