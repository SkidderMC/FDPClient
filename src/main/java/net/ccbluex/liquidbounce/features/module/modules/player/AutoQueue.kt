/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle

/**
 * Sends a configurable command shortly after joining a world – handy for servers with
 * queues/minigames where you re-run a command (e.g. /play, /queue) on every (re)join.
 */
object AutoQueue : Module("AutoQueue", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val command by text("Command", "/play")
        .describe("Command to send after joining a world.")
    private val delay by int("Delay", 1500, 0..15000)
        .describe("Delay after joining before sending command.")
    private val repeatOnRejoin by boolean("RepeatOnRejoin", true)
        .describe("Resend the command each time you rejoin.")
    private val notify by boolean("Notify", true)
        .describe("Send a chat message when the command is sent.")

    private val matchTriggers by boolean("MatchTriggers", false)
        .describe("Also act on server messages matching keywords, not only after joining.")
    private val keywords by text("Keywords", "queue,play again,new game") { matchTriggers }
        .describe("Comma-separated words; a match runs the trigger action.")
    private val matchChat by boolean("MatchChat", true) { matchTriggers }
        .describe("Match against chat messages.")
    private val matchTitle by boolean("MatchTitle", true) { matchTriggers }
        .describe("Match against title text.")
    private val matchSubtitle by boolean("MatchSubtitle", true) { matchTriggers }
        .describe("Match against subtitle text.")
    private val triggerAction by choices("TriggerAction", arrayOf("SendCommand", "UseItem"), "SendCommand") { matchTriggers }
        .describe("What to do when a keyword matches.")
    private val triggerCooldown by int("TriggerCooldown", 3000, 0..30000) { matchTriggers }
        .describe("Minimum delay between keyword-triggered actions.")

    private var joinTime = 0L
    private var pending = false
    private val triggerTimer = MSTimer()

    val onWorld = handler<WorldEvent> { event ->
        if (event.worldClient != null && repeatOnRejoin) {
            joinTime = System.currentTimeMillis()
            pending = true
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!pending) return@handler

        val player = mc.thePlayer ?: return@handler

        if (System.currentTimeMillis() - joinTime >= delay) {
            val cmd = command.trim()
            if (cmd.isNotEmpty()) {
                player.sendChatMessage(cmd)
                if (notify) chat("§8[§9§lAutoQueue§8] §3Sent §a$cmd")
            }
            pending = false
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (!matchTriggers || event.eventType != EventState.RECEIVE) return@handler
        mc.thePlayer ?: return@handler

        val text = when (val packet = event.packet) {
            is S02PacketChat -> if (matchChat) packet.chatComponent?.unformattedText else null
            is S45PacketTitle -> when (packet.type) {
                S45PacketTitle.Type.TITLE -> if (matchTitle) packet.message?.unformattedText else null
                S45PacketTitle.Type.SUBTITLE -> if (matchSubtitle) packet.message?.unformattedText else null
                else -> null
            }
            else -> null
        }?.lowercase() ?: return@handler

        val words = keywords.split(',', '\n').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (words.none { text.contains(it) }) return@handler
        if (!triggerTimer.hasTimePassed(triggerCooldown.toLong())) return@handler

        performTriggerAction()
        triggerTimer.reset()
    }

    private fun performTriggerAction() {
        val player = mc.thePlayer ?: return

        when (triggerAction) {
            "SendCommand" -> {
                val cmd = command.trim()
                if (cmd.isNotEmpty()) {
                    player.sendChatMessage(cmd)
                    if (notify) chat("§8[§9§lAutoQueue§8] §3Triggered §a$cmd")
                }
            }

            "UseItem" -> {
                player.heldItem?.let {
                    player.sendUseItem(it)
                    if (notify) chat("§8[§9§lAutoQueue§8] §3Triggered §aUseItem")
                }
            }
        }
    }

    override fun onEnable() {
        // Fire once on enable too, in case we are already in a world
        if (mc.theWorld != null) {
            joinTime = System.currentTimeMillis()
            pending = true
        }
        super.onEnable()
    }
}
