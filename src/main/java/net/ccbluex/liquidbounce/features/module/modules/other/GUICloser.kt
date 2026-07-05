/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory

/**
 * Automatically closes container screens (chests, shops, forced server menus) a short delay after
 * they open. Useful against anti-AFK menus and servers that pop GUIs in your face.
 */
object GUICloser : Module("GUICloser", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val delay by int("Delay", 200, 0..5000)
        .describe("Delay before closing a screen in milliseconds.")
    private val includeInventory by boolean("IncludeInventory", false)
        .describe("Also close your own inventory screen.")
    private val printScreenTitle by boolean("PrintScreenTitle", false)
        .describe("Print the title of each closed screen to chat.")
    private val mode by choices("Mode", arrayOf("All", "Matches", "Contains"), "All")
        .describe("Close all screens, or only those whose title matches the filter.")
    private val filter by text("Filter", "") { mode != "All" }
        .describe("Comma-separated titles or regex to match when not in All mode.")

    private var openAt = -1L

    val onUpdate = handler<UpdateEvent> {
        mc.thePlayer ?: return@handler
        val screen = mc.currentScreen

        val target = screen is GuiContainer && (includeInventory || screen !is GuiInventory)

        if (!target) {
            openAt = -1L
            return@handler
        }

        if (openAt < 0L) {
            openAt = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - openAt >= delay) {
            val title = (if (screen is GuiChest) screen.lowerChestInventory?.name else screen.javaClass.simpleName)
                ?: screen.javaClass.simpleName

            if (mode != "All" && !titleMatches(title)) return@handler

            if (printScreenTitle) {
                chat("§3Closed screen: §7$title")
            }
            mc.displayGuiScreen(null)
            openAt = -1L
        }
    }

    private fun titleMatches(title: String): Boolean {
        val patterns = filter.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }
        if (patterns.isEmpty()) return false

        return patterns.any { pattern ->
            runCatching {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                if (mode == "Matches") regex.matches(title) else regex.containsMatchIn(title)
            }.getOrDefault(title.contains(pattern, ignoreCase = true))
        }
    }

    override fun onEnable() {
        openAt = -1L
        super.onEnable()
    }
}
