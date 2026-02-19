/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.inventory

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.other.NoSlotSet
import net.ccbluex.liquidbounce.features.module.modules.visual.SilentHotbarModule
import net.ccbluex.liquidbounce.features.module.modules.other.ChestAura
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.block.BlockBush
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems

object InventoryUtils : MinecraftInstance, Listenable {

    // ──────────────────────────────────────────────
    //  Inventory Cache
    // ──────────────────────────────────────────────

    /**
     * Cache state indicating whether the cached inventory snapshot is still valid.
     *
     * | State           | Meaning                                              |
     * |-----------------|------------------------------------------------------|
     * | `UNINITIALIZED` | No snapshot has been taken yet (fresh login / world) |
     * | `SYNCED`        | Cache matches the live inventory — safe to read      |
     * | `CHANGED`       | Inventory was modified since last sync — re-query    |
     *
     * @author itsakc-me
     */
    enum class CacheState {
        UNINITIALIZED,
        SYNCED,
        CHANGED
    }

    /** Current cache validity state. */
    @JvmStatic
    var cacheState: CacheState = CacheState.UNINITIALIZED
        private set

    /**
     * Monotonically increasing revision counter. Bumped every time the cache
     * transitions to [CacheState.CHANGED]. Modules can store the last-seen
     * revision and compare cheaply: `if (cacheRevision != lastRevision) { ... }`
     */
    @JvmStatic
    var cacheRevision: Long = 0L
        private set

    /** Snapshot of every slot's [ItemStack] (slots 0-44 of inventoryContainer). */
    @JvmStatic
    val cachedSlots = arrayOfNulls<ItemStack?>(45)

    /**
     * Takes a full snapshot of the player's inventory container.
     * After this call [cacheState] == [CacheState.SYNCED].
     */
    @JvmStatic
    fun syncCache() {
        val player = mc.thePlayer ?: return
        val container = player.inventoryContainer ?: return

        for (i in 0 until minOf(45, container.inventorySlots.size)) {
            cachedSlots[i] = container.getSlot(i).stack?.copy()
        }

        cacheState = CacheState.SYNCED
    }

    /**
     * Marks the cache as dirty. Cheap — does NOT re-read slots.
     * Consumers will see [CacheState.CHANGED] and can decide to call [syncCache].
     */
    @JvmStatic
    fun invalidateCache() {
        if (cacheState == CacheState.UNINITIALIZED) return
        if (cacheState != CacheState.CHANGED) {
            cacheState = CacheState.CHANGED
            cacheRevision++
        }
    }

    /** Resets to [CacheState.UNINITIALIZED] and clears all cached data. */
    @JvmStatic
    fun resetCache() {
        cachedSlots.fill(null)
        cacheState = CacheState.UNINITIALIZED
        cacheRevision++
    }

    // ── Cache query helpers ──────────────────────

    /**
     * Returns the cached [ItemStack] at the given container slot index,
     * or `null` if the slot is empty or cache is uninitialized.
     */
    @JvmStatic
    fun getCachedSlot(index: Int): ItemStack? {
        if (cacheState == CacheState.UNINITIALIZED || index !in cachedSlots.indices) return null
        return cachedSlots[index]
    }

    /**
     * Finds all slot indices in [range] whose cached stack satisfies [predicate].
     */
    @JvmStatic
    inline fun findCachedSlots(range: IntRange, predicate: (ItemStack?) -> Boolean): List<Int> {
        if (cacheState == CacheState.UNINITIALIZED) return emptyList()
        return range.filter { it in cachedSlots.indices && predicate(cachedSlots[it]) }
    }

    /**
     * Returns the first slot index in [range] whose cached stack satisfies [predicate],
     * or `null` if none match.
     */
    @JvmStatic
    inline fun findFirstCachedSlot(range: IntRange, predicate: (ItemStack?) -> Boolean): Int? {
        if (cacheState == CacheState.UNINITIALIZED) return null
        return range.firstOrNull { it in cachedSlots.indices && predicate(cachedSlots[it]) }
    }

    /**
     * Counts how many slots in [range] satisfy [predicate] on the cached snapshot.
     */
    @JvmStatic
    inline fun countCachedSlots(range: IntRange, predicate: (ItemStack?) -> Boolean): Int {
        if (cacheState == CacheState.UNINITIALIZED) return 0
        return range.count { it in cachedSlots.indices && predicate(cachedSlots[it]) }
    }

    /**
     * Whether any hotbar slot (36-44) is empty in the cached snapshot.
     * Falls back to live check if cache is uninitialized.
     */
    @JvmStatic
    fun cachedHasSpaceInHotbar(): Boolean {
        if (cacheState == CacheState.UNINITIALIZED) return hasSpaceInHotbar()
        return (36..44).any { cachedSlots[it] == null }
    }

    /**
     * Quick check: does the cache contain at least one stack matching [item] in [range]?
     */
    @JvmStatic
    fun cachedContainsItem(range: IntRange, item: Item): Boolean {
        if (cacheState == CacheState.UNINITIALIZED) return false
        return range.any { it in cachedSlots.indices && cachedSlots[it]?.item == item }
    }

    // ──────────────────────────────────────────────
    //  Server-side inventory state
    // ──────────────────────────────────────────────

    // Is inventory open on server-side?
    var serverOpenInventory
        get() = _serverOpenInventory
        set(value) {
            if (value != _serverOpenInventory) {
                sendPacket(
                    if (value) C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT)
                    else C0DPacketCloseWindow(mc.thePlayer?.openContainer?.windowId ?: 0)
                )

                _serverOpenInventory = value
            }
        }

    var serverOpenContainer = false
        private set

    // Backing fields
    private var _serverOpenInventory = false

    var lerpedSlot = 0f

    var isFirstInventoryClick = true

    var timeSinceClosedInventory = 0L

    val CLICK_TIMER = MSTimer()

    val BLOCK_BLACKLIST = setOf(
        Blocks.chest,
        Blocks.ender_chest,
        Blocks.trapped_chest,
        Blocks.anvil,
        Blocks.sand,
        Blocks.web,
        Blocks.torch,
        Blocks.crafting_table,
        Blocks.furnace,
        Blocks.waterlily,
        Blocks.dispenser,
        Blocks.stone_pressure_plate,
        Blocks.wooden_pressure_plate,
        Blocks.noteblock,
        Blocks.dropper,
        Blocks.tnt,
        Blocks.standing_banner,
        Blocks.wall_banner,
        Blocks.redstone_torch,
        Blocks.ladder
    )

    fun findItemArray(startInclusive: Int, endInclusive: Int, items: Array<Item>): Int? {
        for (i in startInclusive..endInclusive)
            if (mc.thePlayer.openContainer.getSlot(i).stack?.item in items)
                return i - 36

        return null
    }

    fun findItem(start: Int, end: Int, item: Item): Int? {
        for (i in start..end)
            if (mc.thePlayer.openContainer.getSlot(i).stack?.item == item)
                return i - if (start == 36 && end == 44) 36 else 0

        return null
    }

    fun hasSpaceInHotbar(): Boolean {
        for (i in 36..44)
            mc.thePlayer.openContainer.getSlot(i).stack ?: return true

        return false
    }

    fun hasSpaceInInventory() = mc.thePlayer?.inventory?.firstEmptyStack != -1

    fun countSpaceInInventory() = mc.thePlayer.inventory.mainInventory.count { it.isEmpty() }

    fun findBlockInHotbar(): Int? {
        val player = mc.thePlayer ?: return null
        val inventory = player.openContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

            stack.item is ItemBlock && stack.stackSize > 0 && block !in BLOCK_BLACKLIST && block !is BlockBush
        }.minByOrNull { (inventory.getSlot(it).stack.item as ItemBlock).block.isFullCube }?.minus(36)
    }

    fun findLargestBlockStackInHotbar(): Int? {
        val player = mc.thePlayer ?: return null
        val inventory = player.openContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

            stack.item is ItemBlock && stack.stackSize > 0 && block.isFullCube && block !in BLOCK_BLACKLIST && block !is BlockBush
        }.maxByOrNull { inventory.getSlot(it).stack.stackSize }?.minus(36)
    }

    fun findBlockStackInHotbarGreaterThan(amount: Int): Int? {
        val player = mc.thePlayer ?: return null
        val inventory = player.openContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

            stack.item is ItemBlock && stack.stackSize > amount && block.isFullCube && block !in BLOCK_BLACKLIST && block !is BlockBush
        }.minByOrNull { (inventory.getSlot(it).stack.item as ItemBlock).block.isFullCube }?.minus(36)
    }

    // Converts container slot to hotbar slot id, else returns null
    fun Int.toHotbarIndex(stacksSize: Int): Int? {
        val parsed = this - stacksSize + 9

        return if (parsed in 0..8) parsed else null
    }

    fun blocksAmount(): Int {
        val player = mc.thePlayer ?: return 0
        var amount = 0

        for (i in 36..44) {
            val stack = player.inventorySlot(i).stack ?: continue
            val item = stack.item
            if (item is ItemBlock) {
                val block = item.block
                val heldItem = player.heldItem
                if (heldItem != null && heldItem == stack || block !in BLOCK_BLACKLIST && block !is BlockBush) {
                    amount += stack.stackSize
                }
            }
        }

        return amount
    }

    /** Sync cache when player opens their inventory GUI. */
    val onScreen = handler<ScreenEvent> {
        if (it.guiScreen is GuiInventory) {
            syncCache()
        }
    }

    /** Auto-sync on tick if state is CHANGED and inventory screen is still open. */
    val onGameTick = handler<GameTickEvent> {
        if (cacheState == CacheState.CHANGED && mc.currentScreen is GuiInventory) {
            syncCache()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.isCancelled) return@handler

        when (val packet = event.packet) {
            is C08PacketPlayerBlockPlacement, is C0EPacketClickWindow -> {
                CLICK_TIMER.reset()

                if (packet is C0EPacketClickWindow) {
                    isFirstInventoryClick = false
                    // Invalidate cache when player clicks in their inventory
                    if (packet.windowId == 0) invalidateCache()
                }
            }

            // Server updated a single slot in player inventory
            is S2FPacketSetSlot -> {
                if (packet.func_149175_c() == 0) invalidateCache()
            }

            // Server sent full window contents for player inventory
            is S30PacketWindowItems -> {
                if (packet.func_148911_c() == 0) invalidateCache()
            }

            is C16PacketClientStatus ->
                if (packet.status == OPEN_INVENTORY_ACHIEVEMENT) {
                    if (_serverOpenInventory) event.cancelEvent()
                    else {
                        isFirstInventoryClick = true
                        _serverOpenInventory = true
                    }
                }

            is C0DPacketCloseWindow, is S2EPacketCloseWindow, is S2DPacketOpenWindow -> {
                isFirstInventoryClick = false
                _serverOpenInventory = false
                serverOpenContainer = false

                timeSinceClosedInventory = System.currentTimeMillis()

                if (packet is S2DPacketOpenWindow) {
                    if (packet.guiId == "minecraft:chest" || packet.guiId == "minecraft:container")
                        serverOpenContainer = true
                } else
                    ChestAura.tileTarget = null
            }

            is S09PacketHeldItemChange -> {
                if (SilentHotbar.currentSlot == packet.heldItemHotbarIndex)
                    return@handler

                SilentHotbar.ignoreSlotChange = true

                val previousSlot = SilentHotbar.currentSlot

                if (NoSlotSet.handleEvents()) {
                    WaitTickUtils.conditionalSchedule {
                        if (SilentHotbar.currentSlot == packet.heldItemHotbarIndex) {
                            mc.thePlayer?.inventory?.currentItem = previousSlot

                            return@conditionalSchedule true
                        }

                        false
                    }
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        val module = SilentHotbarModule

        val slotToUse = SilentHotbar.renderSlot(module.handleEvents() && module.keepHotbarSlot).toFloat()

        lerpedSlot = (lerpedSlot..slotToUse).lerpWith(RenderUtils.deltaTimeNormalized())
    }

    val onWorld = handler<WorldEvent> {
        SilentHotbar.resetSlot()

        _serverOpenInventory = false
        serverOpenContainer = false

        // Reset cache on world change (server switch / respawn)
        resetCache()
    }
}
