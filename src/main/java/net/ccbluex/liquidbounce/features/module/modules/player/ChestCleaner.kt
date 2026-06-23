/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
@file:Suppress("unused")
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object ChestCleaner : Module("ChestCleaner", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val filter by choices("Filter", arrayOf("Whitelist", "Blacklist"), "Whitelist")
        .describe("Treat the item list as whitelist or blacklist.")
    private val items by text("Items", "")
        .describe("Comma-separated item names to filter by.")
    private val autoClose by boolean("AutoClose", true)
        .describe("Close the chest once cleaning is done.")
    private val chestTitle by boolean("ChestTitle", true)
        .describe("Only clean containers titled as a chest.")

    private val delay by intRange("Delay", 50..100, 0..500)
        .describe("Random delay between item moves in ms.")
    private val startDelay by intRange("StartDelay", 50..100, 0..500)
        .describe("Random delay before cleaning starts in ms.")

    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val timer = MSTimer()
    private var entered = false
    private var started = false

    private fun parsedItems(): Set<String> =
        items.split(",", " ")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun registryName(item: Item): String {
        val location = Item.itemRegistry.getNameForObject(item)?.toString() ?: return ""
        return location.substringAfter(':').lowercase()
    }

    private fun matchesFilter(stack: ItemStack): Boolean {
        val list = parsedItems()
        val name = registryName(stack.item)
        val contained = list.any { it == name || (it.isNotEmpty() && name.contains(it)) }

        return when (filter) {
            "Whitelist" -> contained
            else -> !contained
        }
    }

    private val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val screen = mc.currentScreen

        if (screen !is GuiChest) {
            entered = false
            started = false
            return@handler
        }

        if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
            return@handler

        if (!canClickInventory())
            return@handler

        val container = player.openContainer ?: return@handler
        if (container.windowId == 0)
            return@handler

        val lowerChest = screen.lowerChestInventory ?: return@handler

        if (chestTitle && Blocks.chest.localizedName !in lowerChest.name)
            return@handler

        if (!entered) {
            entered = true
            timer.reset()
            return@handler
        }

        val gate = if (started) delay.random() else startDelay.random()
        if (!timer.hasTimePassed(gate))
            return@handler

        val slots = container.inventorySlots
        val chestSlotCount = slots.size - 36

        if (chestSlotCount <= 0)
            return@handler

        val targetSlot = (0 until chestSlotCount).firstOrNull { index ->
            val stack = slots[index].stack ?: return@firstOrNull false
            stack.stackSize > 0 && matchesFilter(stack)
        }

        if (targetSlot == null) {
            if (autoClose)
                player.closeScreen()
            return@handler
        }

        mc.playerController?.windowClick(container.windowId, targetSlot, 1, 4, player)
        started = true
        timer.reset()
    }

    override fun onDisable() {
        entered = false
        started = false
    }
}
