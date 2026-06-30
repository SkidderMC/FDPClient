/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

object BetterChat : Module(
    "BetterChat",
    Category.OTHER,
    Category.SubCategory.MISCELLANEOUS,
    legacyNames = arrayOf("AntiSpam"),
) {
    private val timestamps by boolean("Timestamps", true)
    private val timestampFormat by text("TimestampFormat", "HH:mm:ss") { timestamps }
    private val stackMessages by boolean("StackMessages", true)
    private val duplicateWindow by int("DuplicateWindow", 30, 1..300, "seconds") { stackMessages }
    private val filtersValue = mutableList("Filters")
        .describe("Regular expressions matched against incoming plain chat text.")
    private val filters by filtersValue

    private data class SeenMessage(var count: Int, var lastSeenAt: Long, val lineId: Int)
    private val seen = LinkedHashMap<String, SeenMessage>()
    private val order = ArrayDeque<String>()

    val onPacket = handler<PacketEvent> { event ->
        if (event.eventType != EventState.RECEIVE || event.packet !is S02PacketChat) return@handler
        val packet = event.packet
        if (packet.type.toInt() == 2) return@handler

        val component = packet.chatComponent
        val plain = component.unformattedText.trim()
        if (plain.isEmpty()) return@handler
        if (filters.any { pattern -> runCatching { Regex(pattern).containsMatchIn(plain) }.getOrDefault(false) }) {
            event.cancelEvent()
            return@handler
        }

        val now = System.currentTimeMillis()
        prune(now)
        val key = plain.lowercase(Locale.ROOT)
        val existing = seen[key]
        val line = when {
            stackMessages && existing != null -> {
                existing.count++
                existing.lastSeenAt = now
                order.remove(key)
                order.addLast(key)
                appendCount(component, existing.count)
            }
            else -> {
                if (stackMessages) {
                    val id = stableLineId(key)
                    seen[key] = SeenMessage(1, now, id)
                    order.addLast(key)
                }
                component
            }
        }

        if (timestamps || stackMessages) {
            event.cancelEvent()
            val rendered = if (timestamps) prependTimestamp(line) else line
            mc.ingameGUI.chatGUI.printChatMessageWithOptionalDeletion(rendered, seen[key]?.lineId ?: 0)
        }
    }

    val onWorld = handler<WorldEvent> {
        seen.clear()
        order.clear()
    }

    private fun prependTimestamp(component: IChatComponent): IChatComponent {
        val format = runCatching { SimpleDateFormat(timestampFormat) }.getOrElse { SimpleDateFormat("HH:mm:ss") }
        return ChatComponentText("§7[${format.format(Date())}] §r").appendSibling(component.createCopy())
    }

    private fun appendCount(component: IChatComponent, count: Int): IChatComponent =
        component.createCopy().appendText(" §7[$count]")

    private fun prune(now: Long) {
        val expiry = duplicateWindow * 1000L
        while (order.isNotEmpty()) {
            val key = order.first
            val entry = seen[key]
            if (entry != null && now - entry.lastSeenAt <= expiry && seen.size <= 200) break
            order.removeFirst()
            seen.remove(key)
        }
    }

    private fun stableLineId(key: String) = (key.hashCode() xor 0x464450).let { if (it == 0) 1 else it }
}
