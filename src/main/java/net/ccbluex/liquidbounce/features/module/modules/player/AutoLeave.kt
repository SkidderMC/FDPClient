/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.timing.MSTimer

/**
 * Leaves the current server automatically when a danger condition is met
 * (low health or too many players nearby). Useful as an anti-death / anti-gank safety net.
 */
object AutoLeave : Module("AutoLeave", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val onLowHealth by boolean("OnLowHealth", true)
        .describe("Leave the server when health gets low.")
    private val health by int("Health", 6, 0..20) { onLowHealth }
        .describe("Health threshold that triggers leaving.")

    private val onCrowd by boolean("OnPlayersNearby", false)
        .describe("Leave when too many players are nearby.")
    private val players by int("Players", 4, 1..30) { onCrowd }
        .describe("Number of nearby players that triggers leaving.")
    private val range by int("Range", 8, 1..40) { onCrowd }
        .describe("Distance used to count nearby players.")

    private val notify by boolean("Notify", true)
        .describe("Send a chat message when leaving.")

    private val mode by choices("Mode", arrayOf("Disconnect", "Command"), "Disconnect")
        .describe("How to leave: disconnect or send a command.")
    private val command by text("Command", "/lobby") { mode == "Command" }
        .describe("Command to send when leaving in command mode.")
    private val delay by int("Delay", 0, 0..10000)
        .describe("Delay before leaving in milliseconds.")

    init {
        group("Triggers", "OnLowHealth", "Health", "OnPlayersNearby", "Players", "Range")
        group("Leaving", "Mode", "Command", "Delay", "Notify")
    }

    private var triggered = false
    private var pending = false
    private var pendingReason: String? = null
    private val delayTimer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (triggered) return@handler

        if (pending) {
            if (delayTimer.hasTimePassed(delay)) leave(pendingReason)
            return@handler
        }

        var reason: String? = null

        if (onLowHealth && !player.isDead && player.health <= health.toFloat()) {
            reason = "low health (${player.health.toInt()}♥)"
        }

        if (reason == null && onCrowd) {
            val count = world.playerEntities.count { it !== player && player.getDistanceToEntity(it) <= range }
            if (count >= players) reason = "$count players nearby"
        }

        if (reason != null) {
            if (delay > 0) {
                pending = true
                pendingReason = reason
                delayTimer.reset()
            } else {
                leave(reason)
            }
        }
    }

    private fun leave(reason: String?) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        triggered = true
        pending = false
        if (notify) chat("§8[§9§lAutoLeave§8] §3Leaving server: $reason")
        if (mode == "Command") {
            player.sendChatMessage(command)
        } else {
            world.sendQuittingDisconnectingPacket()
            mc.displayGuiScreen(null)
        }
    }

    override fun onEnable() {
        triggered = false
        pending = false
        pendingReason = null
        super.onEnable()
    }
}
