/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.network.play.server.S03PacketTimeUpdate
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

object TpsCommand : Command("tps"), Listenable {

    private const val AVG_SIZE = 15
    private val intervals = ArrayDeque<Long>(AVG_SIZE + 1)
    private var lastTimeUpdateAt = -1L
    private var currentTps = Double.NaN
    private var watching = false
    private var tickCounter = 0
    private var periodTicks = 20

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is S03PacketTimeUpdate) {
            val now = System.currentTimeMillis()
            if (lastTimeUpdateAt > 0) {
                val delta = now - lastTimeUpdateAt
                intervals.addLast(delta)
                while (intervals.size > AVG_SIZE) intervals.removeFirst()
                val avgMs = intervals.map { it.toDouble() }.average()
                currentTps = if (avgMs > 0.0 && !avgMs.isNaN()) {
                    val v = 20.0 / (avgMs / 1000.0)
                    min(20.0, max(0.0, v))
                } else Double.NaN
            }
            lastTimeUpdateAt = now
        }
    }

    val onTick = handler<GameTickEvent> {
        if (!watching) return@handler
        tickCounter++
        if (tickCounter >= periodTicks) {
            tickCounter = 0
            if (currentTps.isNaN()) chat("§cTPS not available. Wait a few seconds.")
            else chat("§3Server TPS is §a${"%.2f".format(currentTps)}§3.")
        }
    }

    val onWorld = handler<WorldEvent> {
        intervals.clear()
        lastTimeUpdateAt = -1L
        currentTps = Double.NaN
        tickCounter = 0
    }

    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            if (currentTps.isNaN()) chat("§cTPS not available. Wait a few seconds.")
            else chat("§3Server TPS is §a${"%.2f".format(currentTps)}§3.")
            return
        }
        when (args[1].lowercase()) {
            "watch", "start" -> {
                periodTicks = args.getOrNull(2)?.toIntOrNull()?.let { (it * 20).coerceIn(5, 200) } ?: 20
                watching = true
                tickCounter = 0
                chat("§aTPS watcher started (§7every ${periodTicks / 20}s§a).")
            }
            "stop", "off" -> {
                if (watching) {
                    watching = false
                    chat("§aTPS watcher stopped.")
                } else {
                    chat("§cTPS watcher is not running.")
                }
            }
            "once" -> {
                if (currentTps.isNaN()) chat("§cTPS not available. Wait a few seconds.")
                else chat("§3Server TPS is §a${"%.2f".format(currentTps)}§3.")
            }
            else -> chat("§7Usage: §b.tps §7| §b.tps watch [seconds] §7| §b.tps stop §7| §b.tps once")
        }
    }
}
