/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.TabCompleteUtils
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.ServerObserver

object TpsCommand : Command("tps"), Listenable {

    private val currentTps: Double
        get() = ServerObserver.tps
    private var watching = false
    private var tickCounter = 0
    private var periodTicks = 20

    val onTick = handler<GameTickEvent> {
        if (!watching) return@handler
        tickCounter++
        if (tickCounter >= periodTicks) {
            tickCounter = 0
            if (currentTps.isNaN()) chat("§cTPS not available. Wait a few seconds.")
            else chat("§3Server TPS is §a${"%.2f".format(currentTps)}§3.")
        }
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

    override fun tabComplete(args: Array<String>): List<String> {
        return when (args.size) {
            1 -> TabCompleteUtils.match(args[0], "watch", "start", "stop", "off", "once")
            else -> emptyList()
        }
    }
}
