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
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.ShapedRecipes
import net.minecraft.item.crafting.ShapelessRecipes

/**
 * Automatically lays out the ingredients for a chosen vanilla recipe in an open crafting table and
 * pulls the result, so you can mass-produce an item without dragging items by hand. It only acts on
 * recipes whose layout it can fully resolve and otherwise does nothing, so it never scrambles your
 * inventory.
 *
 * Wildcard metadata constant from the item registry (matches any damage value).
 */
object AutoCrafter : Module("AutoCrafter", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val target by text("Target", "minecraft:stick")
        .describe("Item ID to automatically craft.")
    private val delay by int("Delay", 250, 0..2000)
        .describe("Delay between craft actions in milliseconds.")
    private val craftAll by boolean("CraftAll", true)
        .describe("Take the whole output stack instead of one item.")

    private const val WILDCARD = 32767
    private const val GRID_WIDTH = 3
    private const val RESULT_SLOT = 0
    private const val FIRST_INVENTORY_SLOT = 10

    private val timer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        mc.currentScreen as? GuiCrafting ?: return@handler

        if (!timer.hasTimePassed(delay.toLong())) {
            return@handler
        }

        // Never act while something is held on the cursor — that is how inventories get scrambled.
        if (player.inventory.itemStack != null) {
            return@handler
        }

        val container = player.openContainer ?: return@handler
        val windowId = container.windowId

        val targetItem = Item.getByNameOrId(target) ?: return@handler
        val recipe = findRecipe(targetItem) ?: return@handler
        val layout = recipeLayout(recipe) ?: return@handler

        for ((gridSlot, template) in layout) {
            if (gridSlot >= container.inventorySlots.size) {
                return@handler
            }

            val current = container.getSlot(gridSlot).stack
            if (current != null && matches(current, template)) {
                continue
            }

            val source = findIngredient(container, template) ?: continue

            // Pick up the source stack, drop a single item into the grid cell, return the rest.
            mc.playerController.windowClick(windowId, source, 0, 0, player)
            mc.playerController.windowClick(windowId, gridSlot, 1, 0, player)
            mc.playerController.windowClick(windowId, source, 0, 0, player)
        }

        // Shift-click the result to take the whole stack, or a normal pickup for a single craft.
        mc.playerController.windowClick(windowId, RESULT_SLOT, 0, if (craftAll) 1 else 0, player)

        timer.reset()
    }

    private fun findRecipe(item: Item): IRecipe? {
        for (recipe in CraftingManager.getInstance().recipeList) {
            if (recipe.recipeOutput?.item == item) {
                return recipe
            }
        }
        return null
    }

    /**
     * Maps grid slot index (1..9) to the template ingredient that belongs there, for the recipe
     * shapes we can resolve deterministically. Returns null for anything else (forge/ore recipes).
     */
    private fun recipeLayout(recipe: IRecipe): Map<Int, ItemStack>? {
        when (recipe) {
            is ShapedRecipes -> {
                val width = recipe.recipeWidth
                val height = recipe.recipeHeight
                val items = recipe.recipeItems
                val map = HashMap<Int, ItemStack>()
                for (row in 0 until height) {
                    for (col in 0 until width) {
                        val template = items[row * width + col] ?: continue
                        map[1 + row * GRID_WIDTH + col] = template
                    }
                }
                return if (map.isEmpty()) null else map
            }

            is ShapelessRecipes -> {
                val items = recipe.recipeItems
                if (items.size > 9) {
                    return null
                }
                val map = HashMap<Int, ItemStack>()
                for (index in 0 until items.size) {
                    val template = items[index] as? ItemStack ?: continue
                    map[1 + index] = template
                }
                return if (map.isEmpty()) null else map
            }

            else -> return null
        }
    }

    private fun findIngredient(container: net.minecraft.inventory.Container, template: ItemStack): Int? {
        for (index in FIRST_INVENTORY_SLOT until container.inventorySlots.size) {
            val stack = container.getSlot(index).stack ?: continue
            if (matches(stack, template)) {
                return index
            }
        }
        return null
    }

    private fun matches(stack: ItemStack, template: ItemStack): Boolean {
        if (stack.item != template.item) {
            return false
        }
        return template.metadata == WILDCARD || stack.metadata == template.metadata
    }
}
