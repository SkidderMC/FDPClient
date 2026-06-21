/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.awt.Color

object InventoryTracker : Module("InventoryTracker", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val maxEntries by int("MaxEntries", 6, 1..20)
    private val lifetime by int("Lifetime", 5000, 1000..30000, "ms")
    private val fadeTime by int("FadeTime", 500, 0..3000, "ms")
    private val showSlot by boolean("ShowSlot", true)
    private val showCount by boolean("ShowCount", true)

    private val posX by int("X", 4, 0..400)
    private val posY by int("Y", 60, 0..400)

    private val font by font("Font", Fonts.font20)
    private val shadow by boolean("Shadow", true)

    private class LogEntry(val text: String, val time: Long)

    private val log = ArrayDeque<LogEntry>()

    // Snapshot of the previous inventory state keyed by slot index.
    private val previous = HashMap<Int, ItemSnapshot>()

    private class ItemSnapshot(val name: String, val count: Int, val itemId: Int, val meta: Int)

    private fun snapshot(stack: ItemStack?): ItemSnapshot? {
        if (stack == null || stack.stackSize <= 0) return null
        return ItemSnapshot(
            stack.displayName ?: "Item",
            stack.stackSize,
            Item.getIdFromItem(stack.item),
            stack.metadata
        )
    }

    private fun push(text: String) {
        log.addFirst(LogEntry(text, System.currentTimeMillis()))
        while (log.size > maxEntries) {
            log.removeLast()
        }
    }

    private fun slotLabel(slot: Int): String {
        if (!showSlot) return ""
        return when (slot) {
            in 0..8 -> " §7[hotbar ${slot + 1}]"
            else -> " §7[slot $slot]"
        }
    }

    private fun describe(snap: ItemSnapshot): String {
        return if (showCount && snap.count > 1) "${snap.name} §7x${snap.count}" else snap.name
    }

    private fun reset() {
        log.clear()
        previous.clear()
    }

    override fun onEnable() = reset()
    override fun onDisable() = reset()

    val onWorld = handler<WorldEvent> { reset() }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val inventory = player.inventory ?: return@handler
        val main = inventory.mainInventory ?: return@handler

        for (slot in main.indices) {
            val current = snapshot(main[slot])
            val old = previous[slot]

            when {
                old == null && current != null -> {
                    push("§a+ ${describe(current)}${slotLabel(slot)}")
                }
                old != null && current == null -> {
                    push("§c- ${describe(old)}${slotLabel(slot)}")
                }
                old != null && current != null -> {
                    if (old.itemId != current.itemId || old.meta != current.meta) {
                        push("§b~ ${old.name} §7-> §b${describe(current)}${slotLabel(slot)}")
                    } else if (showCount && old.count != current.count) {
                        val diff = current.count - old.count
                        val sign = if (diff > 0) "§a+$diff" else "§c$diff"
                        push("§b~ ${current.name} §7($sign§7)${slotLabel(slot)}")
                    }
                }
            }

            if (current == null) {
                previous.remove(slot)
            } else {
                previous[slot] = current
            }
        }
    }

    val onRender2D = handler<Render2DEvent> {
        if (mc.thePlayer == null) return@handler

        val now = System.currentTimeMillis()
        val life = lifetime.toLong()
        val fade = fadeTime.toLong().coerceAtMost(life)

        log.removeIf { now - it.time > life }
        if (log.isEmpty()) return@handler

        val sr = ScaledResolution(mc)
        val x = posX.toFloat().coerceAtMost((sr.scaledWidth - 2).toFloat())
        var y = posY.toFloat()
        val lineHeight = font.FONT_HEIGHT + 2f

        for (entry in log) {
            val age = now - entry.time
            val alpha = if (fade > 0 && age > life - fade) {
                ((life - age).toFloat() / fade).coerceIn(0f, 1f)
            } else 1f

            val a = (alpha * 255f).toInt().coerceIn(0, 255)
            if (a <= 0) {
                y += lineHeight
                continue
            }

            val width = font.getStringWidth(entry.text).toFloat()
            val bg = Color(0, 0, 0, (a * 0.5f).toInt().coerceIn(0, 160)).rgb
            drawRect(x - 2f, y - 1f, x + width + 2f, y + font.FONT_HEIGHT + 1f, bg)

            val color = Color(255, 255, 255, a).rgb
            font.drawString(entry.text, x, y, color, shadow)

            y += lineHeight
        }
    }
}
