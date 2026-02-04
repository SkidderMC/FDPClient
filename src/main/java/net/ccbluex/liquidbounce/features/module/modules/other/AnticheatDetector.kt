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
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.ccbluex.liquidbounce.utils.client.ServerUtils.remoteIp

object AnticheatDetector : Module("AntiCheatDetector", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val debug by boolean("Debug", true)
    private val actionNumbers = mutableListOf<Int>()
    private var check = false
    private var ticksPassed = 0

    var detectedACName: String = ""

    val onPacket = handler<PacketEvent> { event ->
        when (event.packet) {
            is S32PacketConfirmTransaction -> {
                if (check) handleTransaction(event.packet.actionNumber.toInt())
            }
            is S01PacketJoinGame -> reset().also { check = true }
        }
    }

    val onTick = handler<GameTickEvent> {
        if (check && ticksPassed++ > 40) {
            notify("None").also { reset() }
        }
    }

    private fun handleTransaction(action: Int) {
        actionNumbers.add(action).also { if (debug) chat("ID: $action") }
        ticksPassed = 0
        if (actionNumbers.size >= 5) analyzeActionNumbers()
    }

    private fun analyzeActionNumbers() {
        val diffs = actionNumbers.windowed(2) { it[1] - it[0] }
        val first = actionNumbers.first()

        when {
            remoteIp.lowercase().equals("hypixel.net", true) -> {
                detectedACName = "Watchdog"
                notify("Watchdog")
            }

            diffs.all { it == diffs.first() } -> when (diffs.first()) {

                1 -> when (first) {
                    in -23772..-23762 -> "Vulcan"
                    in 95..105, in -20005..-19995 -> "Matrix"
                    in -32773..-32762 -> "Grizzly"
                    else -> "Verus"
                }

                -1 -> when {
                    first in -8287..-8280 -> "Errata"
                    first < -3000 -> "Intave"
                    first in -5..0 -> "Grim"
                    first in -3000..-2995 -> "Karhu"
                    else -> "Polar"
                }
                else -> null
            }?.let { acName ->
                detectedACName = acName
                notify(acName)
            }

            actionNumbers.take(2).let { it[0] == it[1] }
                    && actionNumbers.drop(2).windowed(2).all { it[1] - it[0] == 1 }
                -> {
                detectedACName = "Verus"
                notify("Verus")
            }

            diffs.take(2).let { it[0] >= 100 && it[1] == -1 }
                    && diffs.drop(2).all { it == -1 }
                -> {
                detectedACName = "Polar"
                notify("Polar")
            }

            actionNumbers.first() < -3000 && actionNumbers.any { it == 0 }
                -> {
                detectedACName = "Intave"
                notify("Intave")
            }

            actionNumbers.take(3) == listOf(-30767, -30766, -25767)
                    && actionNumbers.drop(3).windowed(2).all { it[1] - it[0] == 1 }
                -> {
                detectedACName = "Old Vulcan"
                notify("Old Vulcan")
            }

            else -> {
                detectedACName = "Unknown"
                notify("Unknown").also { if (debug) logNumbers() }
            }
        }
        reset()
    }

    private fun notify(message: String) =
        hud.addNotification(Notification("Alert", "§3Anticheat detected: $message", Type.WARNING, 3000))


    private fun logNumbers() {
        chat("Action Numbers: ${actionNumbers.joinToString()}")
        chat("Differences: ${actionNumbers.windowed(2) { it[1] - it[0] }.joinToString()}")
    }

    private fun reset() {
        actionNumbers.clear()
        ticksPassed = 0
        check = false
    }

    override fun onEnable() = reset()

    init {
        state = true
    }
}