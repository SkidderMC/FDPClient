/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat

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

    private var joinTime = 0L
    private var pending = false

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

    override fun onEnable() {
        // Fire once on enable too, in case we are already in a world
        if (mc.theWorld != null) {
            joinTime = System.currentTimeMillis()
            pending = true
        }
        super.onEnable()
    }
}
