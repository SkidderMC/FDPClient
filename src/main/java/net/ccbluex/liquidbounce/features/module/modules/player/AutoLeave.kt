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
    private val health by int("Health", 6, 0..20) { onLowHealth }

    private val onCrowd by boolean("OnPlayersNearby", false)
    private val players by int("Players", 4, 1..30) { onCrowd }
    private val range by int("Range", 8, 1..40) { onCrowd }

    private val notify by boolean("Notify", true)

    private val mode by choices("Mode", arrayOf("Disconnect", "Command"), "Disconnect")
    private val command by text("Command", "/lobby") { mode == "Command" }
    private val delay by int("Delay", 0, 0..10000)

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
