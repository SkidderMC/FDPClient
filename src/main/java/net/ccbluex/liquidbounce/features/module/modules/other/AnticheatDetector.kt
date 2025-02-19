/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S01PacketJoinGame

object AnticheatDetector : Module("AntiCheatDetector", Category.OTHER) {

    private val debug by boolean("Debug", true)
    private val actionNumbers = mutableListOf<Int>()
    private var check = false
    private var ticksPassed = 0

    var detectedACName: String = ""

    val onPacket = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (check) {
                    actionNumbers.add(packet.actionNumber.toInt())

                    if (debug) {
                        chat("ID: ${packet.actionNumber}")
                    }

                    if (actionNumbers.size >= 5) {
                        analyzeActionNumbers()
                        check = false
                    }
                    ticksPassed = 0
                }
            }

            is S01PacketJoinGame -> {
                reset()
                check = true
            }
        }
    }

    val onTick = handler<GameTickEvent> {
        if (check) ticksPassed++
        if (ticksPassed > 40 && check) {
            addNotification(Notification("Alert", "§3No Anticheat present", Type.WARNING, 3000))
            check = false
            actionNumbers.clear()
        }
    }

    override fun onEnable() {
        reset()
    }

    private fun analyzeActionNumbers() {
        if (actionNumbers.size < 3) {
            return
        }

        val differences = mutableListOf<Int>()
        for (i in 1 until actionNumbers.size) {
            differences.add(actionNumbers[i] - actionNumbers[i - 1])
        }

        val allSame = differences.all { it == differences[0] }
        if (allSame) {
            val step = differences[0]
            val first = actionNumbers.first()

            val detectedAC = when (step) {
                1 -> when (first) {
                    in -23772..-23762 -> "Vulcan"
                    in 95..105 -> "Matrix"
                    in -20005..-19995 -> "Matrix"
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
            }

            detectedAC?.let {
                detectedACName = it
                addNotification(Notification("§3Anticheat detected: §a$it", "§3Anticheat detected: §a$it", Type.WARNING, 3000))
                actionNumbers.clear()
                return
            }
        }

        // Polar
        if (differences.size >= 2) {
            val firstDiff = differences[0]
            val secondDiff = differences[1]
            val remainingDiffs = differences.drop(2)

            if (firstDiff >= 100 && secondDiff == -1 && remainingDiffs.all { it == -1 }) {
                detectedACName = "Polar"
                addNotification(Notification("Alert", "§3Anticheat detected: §aPolar", Type.WARNING, 3000))
                actionNumbers.clear()
                return
            }
        }

        // Intave zero handling
        val firstAction = actionNumbers.firstOrNull()
        if (firstAction != null && firstAction < -3000 && actionNumbers.any { it == 0 }) {
            detectedACName = "Intave"
            addNotification(Notification("Alert", "§3Anticheat detected: §aIntave", Type.WARNING, 3000))
            actionNumbers.clear()
            return
        }

        // Old Vulcan
        if (actionNumbers.take(3) == listOf(-30767, -30766, -25767) &&
            actionNumbers.drop(3).zipWithNext().all { (prev, curr) -> curr - prev == 1 }) {
            addNotification(Notification("Alert", "§3Anticheat detected: §aOld Vulcan", Type.WARNING, 3000))
            actionNumbers.clear()
            return
        }
        
        addNotification(Notification("ERROR", "§3No known anticheat detected.", Type.ERROR, 3000))
        if (debug) {
            chat("§3Action Numbers: ${actionNumbers.joinToString()}")
            chat("§3Differences: ${differences.joinToString()}")
        }
        detectedACName = ""
        actionNumbers.clear()
    }

    private fun reset() {
        actionNumbers.clear()
        ticksPassed = 0
        check = false
        detectedACName = ""
    }

    init {
        state = true
    }
}
