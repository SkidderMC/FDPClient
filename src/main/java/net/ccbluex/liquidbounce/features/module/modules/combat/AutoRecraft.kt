package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.Container
import net.minecraft.item.Item
import org.lwjgl.input.Keyboard

/**
 * AutoRecraft Module
 *
 * Automatically crafts items when pressing J key.
 * Supports: Cocoa Beans (green dye), Cactus (green dye), Mushroom Stew
 * Priority: Cocoa Beans > Cactus > Mushrooms
 * Uses the 2x2 crafting grid from the player's inventory.
 */
object AutoRecraft : Module("AutoRecraft", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    // Configuration
    private val delayMs by int("Delay", 50, 0..500)
    private val recraftKeyName by text("RecraftKey", "J")

    // Recraft type options
    private val useCocoa by boolean("UseCocoa", true)
    private val useCactus by boolean("UseCactus", true)
    private val useMushroom by boolean("UseMushroom", true)

    // Crafting grid slots in player inventory
    private const val CRAFT_RESULT = 0    // Craft result slot
    private const val CRAFT_SLOT_1 = 1    // First ingredient
    private const val CRAFT_SLOT_2 = 2    // Second ingredient (for mushroom only)
    private const val CRAFT_SLOT_3 = 3    // Bowl
    private const val CRAFT_SLOT_4 = 4    // Empty slot
    private const val MAX_CRAFT_ATTEMPTS = 5
    private const val STAGE_WAITING_INVENTORY = -1  // Wait for inventory to be ready

    // Recraft types
    private enum class RecraftType {
        NONE,
        COCOA_BEANS,
        CACTUS,
        MUSHROOM
    }

    // State management
    private var isCrafting = false
    private var craftingStage = 0
    private var craftAttempts = 0
    private var lastActionTime = 0L
    private var currentRecraftType = RecraftType.NONE

    // Detected item slots
    private var ingredientSlot1 = -1
    private var ingredientSlot2 = -1
    private var bowlSlot = -1

    val onKey = handler<KeyEvent> { event ->
        val recraftKey = Keyboard.getKeyIndex(recraftKeyName.uppercase())
        if (event.key == recraftKey && mc.currentScreen == null && mc.thePlayer != null) {
            startCrafting()
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (!isCrafting) return@handler

        if (mc.currentScreen !is GuiInventory) {
            stopCrafting()
            return@handler
        }


        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime >= delayMs) {
            lastActionTime = currentTime

            if (craftingStage == STAGE_WAITING_INVENTORY) {
                findItems()
                if (isCrafting) {
                    craftingStage = 0
                }
                return@handler
            }

            processCrafting(player.openContainer)
        }
    }

    /**
     * Starts the crafting process by opening inventory and finding items.
     */
    private fun startCrafting() {
        val player = mc.thePlayer ?: return

        mc.displayGuiScreen(GuiInventory(player))

        isCrafting = true
        craftingStage = STAGE_WAITING_INVENTORY
        lastActionTime = 0L
        craftAttempts = 0
        currentRecraftType = RecraftType.NONE

        ingredientSlot1 = -1
        ingredientSlot2 = -1
        bowlSlot = -1
    }

    /**
     * Finds items in inventory and determines the best recraft type.
     * Priority: Cocoa Beans > Cactus > Mushrooms
     */
    private fun findItems() {
        val player = mc.thePlayer ?: return
        val container = player.openContainer

        // Temporary slots for each type
        var cocoaSlot = -1
        var cactusSlot = -1
        var mushroomBrownSlot = -1
        var mushroomRedSlot = -1
        var foundBowlSlot = -1

        for (i in 9 until container.inventorySlots.size) {
            val slot = container.getSlot(i)

            if (!slot.hasStack) continue

            val stack = slot.stack
            val item = stack.item

            when {
                item == Items.dye && stack.metadata == 3 && cocoaSlot == -1 -> {
                    cocoaSlot = i
                }
                item == Item.getItemFromBlock(Blocks.cactus) && cactusSlot == -1 -> {
                    cactusSlot = i
                }
                item == Item.getItemFromBlock(Blocks.brown_mushroom) && mushroomBrownSlot == -1 -> {
                    mushroomBrownSlot = i
                }
                item == Item.getItemFromBlock(Blocks.red_mushroom) && mushroomRedSlot == -1 -> {
                    mushroomRedSlot = i
                }
                item == Items.bowl && foundBowlSlot == -1 -> {
                    foundBowlSlot = i
                }
            }
        }

        when {
            useCocoa && cocoaSlot != -1 && foundBowlSlot != -1 -> {
                currentRecraftType = RecraftType.COCOA_BEANS
                ingredientSlot1 = cocoaSlot
                bowlSlot = foundBowlSlot
            }
            useCactus && cactusSlot != -1 && foundBowlSlot != -1 -> {
                currentRecraftType = RecraftType.CACTUS
                ingredientSlot1 = cactusSlot
                bowlSlot = foundBowlSlot
            }
            useMushroom && mushroomBrownSlot != -1 && mushroomRedSlot != -1 && foundBowlSlot != -1 -> {
                currentRecraftType = RecraftType.MUSHROOM
                ingredientSlot1 = mushroomBrownSlot
                ingredientSlot2 = mushroomRedSlot
                bowlSlot = foundBowlSlot
            }
            else -> {
                stopCrafting()
                mc.thePlayer?.closeScreen()
                return
            }
        }
    }

    /**
     * Processes each crafting stage based on the current recraft type.
     */
    private fun processCrafting(container: Container) {
        when (currentRecraftType) {
            RecraftType.COCOA_BEANS, RecraftType.CACTUS -> processSimpleCrafting(container)
            RecraftType.MUSHROOM -> processMushroomCrafting(container)
            RecraftType.NONE -> stopCrafting()
        }
    }

    /**
     * Processes simple crafting (Cocoa/Cactus + Bowl -> 2 slots only)
     */
    private fun processSimpleCrafting(container: Container) {
        when (craftingStage) {
            0 -> {
                if (ingredientSlot1 != -1) {
                    clickSlot(ingredientSlot1, 1, 0)
                    craftingStage++
                }
            }
            1 -> {
                clickSlot(CRAFT_SLOT_1, 0, 0)
                craftingStage++
            }
            2 -> {
                if (bowlSlot != -1) {
                    clickSlot(bowlSlot, 1, 0)
                    craftingStage++
                }
            }
            3 -> {
                clickSlot(CRAFT_SLOT_2, 0, 0)
                craftingStage++
            }
            4 -> {
                val resultSlot = container.getSlot(CRAFT_RESULT)
                if (resultSlot.hasStack) {
                    if (craftAttempts >= MAX_CRAFT_ATTEMPTS) {
                        craftingStage = 5
                        return
                    }

                    clickSlot(CRAFT_RESULT, 0, 1)
                    craftAttempts++
                } else {
                    craftingStage = 5
                }
            }
            5 -> {
                clickSlot(CRAFT_SLOT_1, 0, 1)
                craftingStage = 6
            }
            6 -> {
                clickSlot(CRAFT_SLOT_2, 0, 1)
                craftingStage = 7
            }
            7 -> {
                clickSlot(CRAFT_SLOT_3, 0, 1)
                craftingStage = 8
            }
            8 -> {
                clickSlot(CRAFT_SLOT_4, 0, 1)
                craftingStage = 9
            }
            9 -> {
                stopCrafting()
                mc.thePlayer?.closeScreen()
            }
        }
    }

    /**
     * Processes mushroom stew crafting (requires 2 mushrooms + bowl)
     */
    private fun processMushroomCrafting(container: Container) {
        when (craftingStage) {
            0 -> {
                if (ingredientSlot1 != -1) {
                    clickSlot(ingredientSlot1, 1, 0)
                    craftingStage++
                }
            }
            1 -> {
                clickSlot(CRAFT_SLOT_1, 0, 0)
                craftingStage++
            }
            2 -> {
                if (ingredientSlot2 != -1) {
                    clickSlot(ingredientSlot2, 1, 0)
                    craftingStage++
                }
            }
            3 -> {
                clickSlot(CRAFT_SLOT_2, 0, 0)
                craftingStage++
            }
            4 -> {
                if (bowlSlot != -1) {
                    clickSlot(bowlSlot, 1, 0)
                    craftingStage++
                }
            }
            5 -> {
                clickSlot(CRAFT_SLOT_3, 0, 0)
                craftingStage++
            }
            6 -> {
                val resultSlot = container.getSlot(CRAFT_RESULT)

                if (resultSlot.hasStack) {
                    if (craftAttempts >= MAX_CRAFT_ATTEMPTS) {
                        craftingStage = 7
                        return
                    }

                    clickSlot(CRAFT_RESULT, 0, 1)
                    craftAttempts++
                } else {
                    craftingStage = 7
                }
            }
            7 -> {
                clickSlot(CRAFT_SLOT_1, 0, 1)
                craftingStage = 8
            }
            8 -> {
                clickSlot(CRAFT_SLOT_2, 0, 1)
                craftingStage = 9
            }
            9 -> {
                clickSlot(CRAFT_SLOT_3, 0, 1)
                craftingStage = 10
            }
            10 -> {
                clickSlot(CRAFT_SLOT_4, 0, 1)
                craftingStage = 11
            }
            11 -> {
                stopCrafting()
                mc.thePlayer?.closeScreen()
            }
        }
    }

    /**
     * Performs a window click operation on the specified slot.
     *
     * @param slotId The slot ID to click
     * @param mouseButton 0 = left click, 1 = right click
     * @param mode 0 = normal, 1 = shift+click, 4 = drop
     */
    private fun clickSlot(slotId: Int, mouseButton: Int, mode: Int) {
        val player = mc.thePlayer ?: return
        val container = player.openContainer ?: return

        mc.playerController?.windowClick(
            container.windowId,
            slotId,
            mouseButton,
            mode,
            player
        )
    }

    /**
     * Resets all crafting state variables.
     */
    private fun stopCrafting() {
        isCrafting = false
        lastActionTime = 0L
        craftingStage = 0
        craftAttempts = 0
        currentRecraftType = RecraftType.NONE
    }

    override fun onDisable() {
        stopCrafting()
    }

    override val tag: String?
        get() = when (currentRecraftType) {
            RecraftType.COCOA_BEANS -> "Cocoa"
            RecraftType.CACTUS -> "Cactus"
            RecraftType.MUSHROOM -> "Mushroom"
            RecraftType.NONE -> null
        }
}
