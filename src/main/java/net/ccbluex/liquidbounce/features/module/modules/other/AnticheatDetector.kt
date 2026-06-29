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

object AnticheatDetector : Module("AntiCheatDetector", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val debug by boolean("Debug", true)
        .describe("Print transaction action numbers to chat for debugging.")
    private var check = false
    private var ticksPassed = 0

    var detectedACName: String = ""

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is S01PacketJoinGame) {
            reset()
            check = true
        }
    }

    val onTick = handler<GameTickEvent> {
        if (check && ticksPassed++ > 40) {
            val result = ServerObserver.guessAnticheat(remoteIp) ?: "Unknown"
            detectedACName = result
            notify(result)
            if (debug && result == "Unknown") logNumbers()
            reset()
        }
    }

    private fun notify(message: String) =
        hud.addNotification(Notification("Alert", "§3Anticheat detected: $message", Type.WARNING, 3000))


    private fun logNumbers() {
        val actions = ServerObserver.transactions
        chat("Action Numbers: ${actions.joinToString()}")
        chat("Differences: ${actions.windowed(2) { it[1] - it[0] }.joinToString()}")
    }

    private fun reset() {
        ticksPassed = 0
        check = false
    }

    override fun onEnable() = reset()

    init {
        state = true
    }
}
