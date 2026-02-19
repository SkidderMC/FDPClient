/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CacheState
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CLICK_TIMER
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items

/**
 * Refill module - Automatically refills items
 *
 * Automatically refills your hotbar with items from your inventory,
 * such as potions or soups, when the hotbar slots are empty.
 *
 * @author itsakc-me
 */
object Refill : Module("Refill", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {
    private val instant by boolean("Instant", false)
    private val delay by int("Delay", 50, 50..500) { !instant }
    
    private val silent by boolean("Silent", false)

    private val autoClose by boolean("AutoClose", false) { !silent }
    private val autoCloseDelay by int("CloseDelay", 10, 0..20) { autoClose }

    private val mode by choices("Mode", arrayOf("Soup", "Potion"), "Soup")

    private var shouldCloseInventory = false
    private val closeTimer = TickTimer()

    // Track cache revision to skip redundant scans
    private var lastCacheRevision = -1L
    private var cachedRefillSlots = emptyList<Int>()
    
    // List of Metadata values for Healing and Regeneration potions in 1.8.9
    // Includes: Healing I/II, Regen I/II, and their Splash variations
    private val healPotionMetadata = arrayOf(
        8193, 8257, 8225, // Regeneration I, Extended, II
        16385, 16449, 16417, // Splash Regeneration I, Extended, II
        8197, 8229, // Healing I, II
        16389, 16421 // Splash Healing I, II
    )

    override val tag: String
        get() = mode

    override fun onDisable() {
        closeTimer.reset()
        lastCacheRevision = -1L
        cachedRefillSlots = emptyList()
    }

    val onTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (!instant && !CLICK_TIMER.hasTimePassed(delay))
            return@handler

        if (!silent && mc.currentScreen !is GuiInventory)
            return@handler

        if (!canClickInventory())
            return@handler

        // TODO: We want the Cache system here in [Refill]
        // Ensure cache is up-to-date (cheap if already synced)
        if (InventoryUtils.cacheState != CacheState.SYNCED) {
            InventoryUtils.syncCache()
        }

        // Rebuild refill slot list only when cache revision changes
        val currentRevision = InventoryUtils.cacheRevision
        if (currentRevision != lastCacheRevision) {
            lastCacheRevision = currentRevision
            cachedRefillSlots = InventoryUtils.findCachedSlots(9..35) { stack ->
                stack != null && when (mode) {
                    "Soup" -> stack.item == Items.mushroom_stew
                    "Potion" -> stack.item == Items.potionitem && healPotionMetadata.contains(stack.metadata)
                    else -> false
                }
            }
        }

        // Use cached hotbar space check
        if (!InventoryUtils.cachedHasSpaceInHotbar())
            return@handler

        val pendingSlots = cachedRefillSlots.toMutableList()
        for (slot in pendingSlots) {
            // Double-check live slot validity (cache may lag behind by one tick)
            val itemStack = thePlayer.inventoryContainer.getSlot(slot).stack ?: continue

            val isValidItem = when (mode) {
                "Soup" -> itemStack.item == Items.mushroom_stew
                "Potion" -> itemStack.item == Items.potionitem && healPotionMetadata.contains(itemStack.metadata)
                else -> false
            }

            if (isValidItem) {
                click(slot, thePlayer)

                // Invalidate cache after we moved an item
                InventoryUtils.invalidateCache()

                // If it's not instant, break to wait for [delay] tick
                if (!instant)
                    return@handler
            }
        }

        if (silent && mc.currentScreen !is GuiInventory)
            serverOpenInventory = false

        if (autoClose && shouldCloseInventory && closeTimer.hasTimePassed(autoCloseDelay)) {
            if (mc.currentScreen is GuiInventory) {
                mc.thePlayer?.closeScreen()
            }

            shouldCloseInventory = false
            closeTimer.reset()
        }
    }

    fun click(slot: Int, thePlayer: EntityPlayerSP) {
        if (silent && mc.currentScreen !is GuiInventory) serverOpenInventory = true
        else shouldCloseInventory = true

        // Reset close timer on each click for better timing
        closeTimer.reset()

        // If we send multiple packets in one tick, it can cause desyncs
        // So instead we simulate window clicks
        mc.playerController.windowClick(0, slot, 0, 1, thePlayer)
    }
}
