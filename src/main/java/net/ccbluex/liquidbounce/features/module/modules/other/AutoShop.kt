/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.inventory.GuiChest

/**
 * AutoShop module
 *
 * Automatically buys items from a server shop GUI (a chest-style menu) by clicking
 * the slots you configure. Useful for menu-driven shops where each item sits in a
 * fixed slot. Slots are clicked one at a time on a configurable delay.
 *
 * Configure the "Slots" value with a comma-separated list of slot indices to click,
 * e.g. "11,13,15". Optionally restrict activation to GUIs whose title contains a
 * given text via "TitleFilter".
 */
object AutoShop : Module("AutoShop", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val slotsValue by text("Slots", "")
        .describe("Comma-separated slot indices to click in the GUI.")

    private val clickButton by int("ClickButton", 0, 0..1)
        .describe("Mouse button used for each click.")
    private val clickMode by int("ClickMode", 0, 0..6)
        .describe("Container click mode passed to the server.")
    private val delay by int("Delay", 150, 0..1000, "ms")
        .describe("Delay between slot clicks in milliseconds.")

    private val repeatCycles by boolean("Repeat", false)
        .describe("Repeat the slot list multiple times.")
    private val maxCycles by int("MaxCycles", 1, 1..64) { repeatCycles }
        .describe("Maximum number of passes over the slot list.")

    private val onlyWithTitle by boolean("TitleFilter", false)
        .describe("Only act on GUIs whose title matches a filter.")
    private val titleContains by text("TitleContains", "Shop") { onlyWithTitle }
        .describe("Text the GUI title must contain to activate.")

    private val autoClose by boolean("AutoClose", false)
        .describe("Close the GUI once all clicks are done.")

    init {
        group("SlotList", "Slots", "Repeat", "MaxCycles")
        group("Click", "ClickButton", "ClickMode", "Delay")
        group("Title", "TitleFilter", "TitleContains")
        group("Misc", "AutoClose")
    }

    private val timer = MSTimer()

    // Index into the parsed slot list of the next slot to click
    private var clickIndex = 0

    // How many full passes over the slot list we have already completed
    private var completedCycles = 0

    override fun onEnable() {
        reset()
    }

    override fun onDisable() {
        reset()
    }

    private fun reset() {
        timer.reset()
        clickIndex = 0
        completedCycles = 0
    }

    private fun parseSlots(): List<Int> {
        return slotsValue.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
    }

    private fun titleMatches(screen: GuiChest): Boolean {
        if (!onlyWithTitle) {
            return true
        }

        val inventory = screen.lowerChestInventory ?: return false
        return inventory.name.contains(titleContains, ignoreCase = true)
    }

    val onGameTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        val screen = mc.currentScreen as? GuiChest ?: run {
            reset()
            return@handler
        }

        if (!titleMatches(screen)) {
            return@handler
        }

        val slots = parseSlots()
        if (slots.isEmpty()) {
            return@handler
        }

        // Finished the configured passes -> optionally close and stop
        if (clickIndex >= slots.size) {
            val cyclesDone = completedCycles + 1
            val allowMore = repeatCycles && cyclesDone < maxCycles

            if (!allowMore) {
                if (autoClose) {
                    thePlayer.closeScreen()
                }
                reset()
                return@handler
            }

            completedCycles = cyclesDone
            clickIndex = 0
        }

        if (!timer.hasTimePassed(delay)) {
            return@handler
        }

        val container = thePlayer.openContainer ?: return@handler
        val windowId = container.windowId

        val slot = slots[clickIndex]
        if (slot < 0 || slot >= container.inventorySlots.size) {
            // Skip out-of-range slots instead of crashing on a malformed config
            clickIndex++
            return@handler
        }

        mc.playerController.windowClick(windowId, slot, clickButton, clickMode, thePlayer)

        clickIndex++
        timer.reset()
    }
}
