/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CLICK_TIMER
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemStack

/**
 * Replenish module - Automatically refills hotbar items
 *
 * Tops up the held item and other hotbar slots with matching stacks from the
 * rest of the inventory when their count drops below a threshold, or restores
 * an item into a slot that just ran out (e.g. blocks, food).
 *
 * @author itsakc-me
 */
object Replenish : Module("Replenish", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val instant by boolean("Instant", false)
        .describe("Replenish all slots in one tick.")
    private val delay by int("Delay", 50, 0..1000, "ms") { !instant }
        .describe("Delay between replenish clicks in milliseconds.")

    private val itemThreshold by int("ItemThreshold", 8, 0..63)
        .describe("Top up a stack when it drops to this count.")
    private val replenishEmpty by boolean("ReplenishEmpty", true)
        .describe("Refill slots that have been fully emptied.")
    private val heldOnly by boolean("HeldOnly", false)
        .describe("Only replenish the currently held slot.")

    private val silent by boolean("Silent", false)
        .describe("Replenish without opening the real inventory.")

    // Last non-empty stack seen in each hotbar slot, used to restore emptied slots
    private val trackedItems = arrayOfNulls<ItemStack>(9)

    override fun onEnable() {
        clearTracked()
    }

    override fun onDisable() {
        clearTracked()
        if (silent && mc.currentScreen !is GuiInventory) serverOpenInventory = false
    }

    private fun clearTracked() {
        for (i in trackedItems.indices) trackedItems[i] = null
    }

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler
        val container = player.openContainer ?: return@handler

        if (!instant && !CLICK_TIMER.hasTimePassed(delay))
            return@handler

        if (!silent && mc.currentScreen !is GuiInventory)
            return@handler

        if (!canClickInventory())
            return@handler

        // Do not move items while something is on the cursor
        if (player.inventory?.itemStack != null)
            return@handler

        val heldSlot = player.inventory?.currentItem ?: 0

        for (hotbarIndex in 0..8) {
            if (heldOnly && hotbarIndex != heldSlot) continue

            // hotbar container slots are 36..44
            val containerSlot = 36 + hotbarIndex
            if (containerSlot >= container.inventorySlots.size) continue

            val currentStack = container.getSlot(containerSlot).stack

            // Determine which item this slot should hold
            val targetStack = when {
                currentStack != null -> currentStack
                replenishEmpty -> trackedItems[hotbarIndex] ?: continue
                else -> {
                    trackedItems[hotbarIndex] = null
                    continue
                }
            }

            // Remember the live item so we can restore the slot once it empties
            if (currentStack != null) trackedItems[hotbarIndex] = currentStack.copy()

            // Only consider stackable items that are actually below the threshold
            if (targetStack.maxStackSize <= 1) continue
            if (currentStack != null && currentStack.stackSize > itemThreshold) continue

            // Find a matching stack elsewhere in the inventory (main inventory slots 9..35)
            val sourceSlot = (9..35).firstOrNull { source ->
                if (source >= container.inventorySlots.size) return@firstOrNull false
                val sourceStack = container.getSlot(source).stack ?: return@firstOrNull false
                sourceStack.isItemEqual(targetStack) &&
                    ItemStack.areItemStackTagsEqual(sourceStack, targetStack) &&
                    sourceStack.stackSize > 0
            } ?: continue

            if (silent && mc.currentScreen !is GuiInventory) serverOpenInventory = true

            // Pickup source stack, drop it onto the hotbar slot, then return leftovers
            mc.playerController.windowClick(0, sourceSlot, 0, 0, player)
            mc.playerController.windowClick(0, containerSlot, 0, 0, player)
            mc.playerController.windowClick(0, sourceSlot, 0, 0, player)

            InventoryUtils.invalidateCache()
            CLICK_TIMER.reset()

            if (!instant) return@handler
        }

        if (silent && mc.currentScreen !is GuiInventory)
            serverOpenInventory = false
    }
}
