/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.canBeRepairedWithOther
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.autoArmorCurrentSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.autoArmorLastSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.hasScheduledInLastLoop
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.passedPostInventoryCloseDelay
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.toHotbarIndex
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.hasItemAgePassed
import net.ccbluex.liquidbounce.utils.timing.TickedActions.awaitTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clickNextTick
import net.ccbluex.liquidbounce.utils.timing.TickedActions.isTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import org.lwjgl.input.Keyboard

private data class ManualArmorRequest(val armorType: Int)

object AutoArmor : Module("AutoArmor", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {
    private val mode by choices("Mode", arrayOf("Fast", "Legit"), "Fast")
    private val delay by intRange("Delay", 50..50, 0..1000)
    private val minItemAge by int("MinItemAge", 0, 0..2000)

    private val invOpen by +InventoryManager.invOpenValue
    private val simulateInventory by +InventoryManager.simulateInventoryValue

    private val postInventoryCloseDelay by +InventoryManager.postInventoryCloseDelayValue
    private val autoClose by +InventoryManager.autoCloseValue
    private val startDelay by +InventoryManager.startDelayValue
    private val closeDelay by +InventoryManager.closeDelayValue

    // Keeps armor coverage while preserving the old piece whenever possible.
    val smartSwap by boolean("SmartSwap", true)
    private val dropOldArmor by boolean("DropOldArmor", false)

    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val hotbar by boolean("Hotbar", true)

    // Sacrifices 1 tick speed for complete undetectability, needed to bypass Vulcan.
    private val delayedSlotSwitch by boolean("DelayedSlotSwitch", true) { hotbar }

    // Prevents AutoArmor from hotbar equipping while any screen is open.
    private val notInContainers by boolean("NotInContainers", false) { hotbar }

    private val manualSwap by boolean("ManualSwap", true)
    private val manualSelection by choices(
        "ManualSelection",
        arrayOf("First", "Best", "BestPercent", "Defense", "Enchantments"),
        "Best"
    ) { manualSwap }
    private val manualCycle by boolean("ManualCycle", false) { manualSwap }
    private val manualUseArmorFilter by boolean("ManualUseArmorFilter", false) { manualSwap }
    private val manualIgnoreMinItemAge by boolean("ManualIgnoreMinItemAge", true) { manualSwap }

    private val helmetBind by bindText("HelmetBind")
    private val chestplateBind by bindText("ChestplateBind")
    private val leggingsBind by bindText("LeggingsBind")
    private val bootsBind by bindText("BootsBind")

    val highlightSlot by +InventoryManager.highlightSlotValue
    val backgroundColor by +InventoryManager.borderColor

    val borderStrength by +InventoryManager.borderStrength
    val borderColor by +InventoryManager.borderColor

    @Volatile
    private var pendingManualArmorRequest: ManualArmorRequest? = null

    override val tag
        get() = mode

    val onKey = handler<KeyEvent> { event ->
        if (!manualSwap || event.key == Keyboard.KEY_NONE || event.key == keyBind) {
            return@handler
        }

        resolveManualArmorType(event.key)?.let {
            pendingManualArmorRequest = ManualArmorRequest(it)
        }
    }

    override fun onDisable() {
        pendingManualArmorRequest = null
        resetHighlight()
    }

    suspend fun handleManualSwapRequest(): Boolean {
        val request = pendingManualArmorRequest ?: return false
        pendingManualArmorRequest = null

        if (!shouldOperate()) {
            resetHighlight()
            return false
        }

        val thePlayer = mc.thePlayer ?: return false
        val stacks = withContext(Dispatchers.Main) {
            thePlayer.openContainer.inventorySlots.map { it.stack }
        }

        val candidate = selectManualCandidate(request.armorType, stacks) ?: return false
        val hasScheduledClick = equipArmorCandidate(
            armorType = request.armorType,
            index = candidate.first,
            stack = candidate.second,
            stacks = stacks,
            ignoreMinAge = manualIgnoreMinItemAge,
            ignoreRepairCheck = true
        )

        if (hasScheduledClick) {
            awaitTicked()
        }

        return hasScheduledClick
    }

    suspend fun equipFromHotbar() {
        if (!shouldOperate(onlyHotbar = true)) {
            resetHighlight()
            return
        }

        val thePlayer = mc.thePlayer ?: return

        var hasClickedHotbar = false

        val stacks = withContext(Dispatchers.Main) {
            thePlayer.openContainer.inventorySlots.map { it.stack }
        }

        val bestArmorSet = ArmorFilter.selectBestArmorSet(stacks) ?: return

        for (armorType in 0..3) {
            val (index, stack) = bestArmorSet[armorType] ?: continue
            val hotbarIndex = index?.toHotbarIndex(stacks.size) ?: continue

            if (isTicked(index) || isTicked(armorType + 5))
                continue

            if (!stack.hasItemAgePassed(minItemAge))
                continue

            val armorPos = getArmorPosition(stack) - 1

            if (thePlayer.inventory.armorInventory[armorPos] != null)
                continue

            hasClickedHotbar = true

            val equippingAction = {
                autoArmorCurrentSlot = hotbarIndex

                SilentHotbar.selectSlotSilently(
                    this,
                    hotbarIndex,
                    immediate = true,
                    render = false,
                    resetManually = true
                )

                sendPacket(C08PacketPlayerBlockPlacement(stack))

                thePlayer.inventory.armorInventory[armorPos] = stack
                thePlayer.inventory.mainInventory[hotbarIndex] = null
            }

            nextTick(action = equippingAction)

            if (delayedSlotSwitch) {
                delay(delay.random().toLong())
            }
        }

        delay(delay.random().toLong())
        awaitTicked()

        if (hasClickedHotbar) {
            nextTick { SilentHotbar.resetSlot(this) }
        }
    }

    suspend fun equipFromInventory() {
        if (!shouldOperate()) {
            resetHighlight()
            return
        }

        val thePlayer = mc.thePlayer ?: return

        for (armorType in 0..3) {
            if (!shouldOperate()) {
                resetHighlight()
                return
            }

            val stacks = withContext(Dispatchers.Main) {
                thePlayer.openContainer.inventorySlots.map { it.stack }
            }

            val candidate = ArmorFilter.getArmorCandidates(stacks, armorType).firstOrNull() ?: continue
            equipArmorCandidate(armorType, candidate.first, candidate.second, stacks)
        }

        awaitTicked()
    }

    fun equipFromHotbarInChest(hotbarIndex: Int?, stack: ItemStack) {
        if (hotbarIndex == null || !canEquipFromChest()) {
            resetHighlight()
            return
        }

        autoArmorCurrentSlot = hotbarIndex

        SilentHotbar.selectSlotSilently(this, hotbarIndex, immediate = true, render = false, resetManually = true)
        sendPacket(C08PacketPlayerBlockPlacement(stack))
    }

    fun canEquipFromChest() = handleEvents() && hotbar && !notInContainers

    private fun selectManualCandidate(armorType: Int, stacks: List<ItemStack?>): Pair<Int, ItemStack>? {
        val candidates = collectManualCandidates(stacks, armorType)
        if (candidates.isEmpty()) {
            return null
        }

        val sortedCandidates = when (manualSelection) {
            "First" -> candidates
            else -> candidates.sortedByDescending { (_, stack) ->
                ArmorFilter.scoreArmorStack(stack, toArmorPriorityMode(manualSelection))
            }
        }

        if (!manualCycle) {
            return sortedCandidates.first()
        }

        val equippedStack = stacks.getOrNull(armorType + 5)
        val currentIndex = sortedCandidates.indexOfFirst { (_, stack) -> stack == equippedStack }
        return sortedCandidates[if (currentIndex == -1) 0 else (currentIndex + 1) % sortedCandidates.size]
    }

    private fun collectManualCandidates(stacks: List<ItemStack?>, armorType: Int): List<Pair<Int, ItemStack>> =
        stacks.mapIndexedNotNull { index, stack ->
            val armor = stack?.item as? ItemArmor ?: return@mapIndexedNotNull null

            if (index <= 8 || armor.armorType != armorType) {
                return@mapIndexedNotNull null
            }

            if (manualUseArmorFilter && !ArmorFilter.isArmorAllowed(stack)) {
                return@mapIndexedNotNull null
            }

            index to stack
        }

    private suspend fun equipArmorCandidate(
        armorType: Int,
        index: Int?,
        stack: ItemStack,
        stacks: List<ItemStack?>,
        ignoreMinAge: Boolean = false,
        ignoreRepairCheck: Boolean = false,
    ): Boolean {
        index ?: return false

        if (isTicked(index) || isTicked(armorType + 5))
            return false

        if (!ignoreMinAge && !stack.hasItemAgePassed(minItemAge))
            return false

        if (!ignoreRepairCheck && canBeRepairedWithOther(stack, stacks))
            return false

        autoArmorCurrentSlot = index

        when (stacks[armorType + 5]) {
            stack -> {
                resetHighlight()
                return false
            }

            null ->
                click(index, 0, 1)

            else -> {
                if (smartSwap) {
                    // Swap the equipped armor back into the source slot instead of dropping it.
                    click(index, 0, 0)
                    click(armorType + 5, 0, 0)
                    click(index, 0, 0)
                } else {
                    val storageSlot = findStorageSlot(stacks, index)

                    when {
                        storageSlot != null -> {
                            click(armorType + 5, 0, 0)
                            click(storageSlot, 0, 0)
                        }

                        dropOldArmor -> click(armorType + 5, 0, 4)

                        else -> return false
                    }

                    click(index, 0, 1)
                }
            }
        }

        return true
    }

    private fun findStorageSlot(stacks: List<ItemStack?>, excludedSlot: Int): Int? =
        stacks.indices.firstOrNull { it > 8 && it != excludedSlot && stacks[it] == null }

    private suspend fun shouldOperate(onlyHotbar: Boolean = false): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (!passedPostInventoryCloseDelay)
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            if (mc.thePlayer?.openContainer?.windowId != 0 && (!onlyHotbar || notInContainers))
                return false

            if (onlyHotbar)
                return hotbar

            if (invOpen && mc.currentScreen !is GuiInventory)
                return false

            if (canClickInventory(closeWhenViolating = true))
                return true

            delay(50)
        }
    }

    private suspend fun click(slot: Int, button: Int, mode: Int, allowDuplicates: Boolean = false) {
        if (!shouldOperate())
            return

        if (simulateInventory || invOpen || this.mode == "Legit")
            serverOpenInventory = true

        if (isFirstInventoryClick) {
            isFirstInventoryClick = false
            delay(startDelay.toLong())
        }

        clickNextTick(slot, button, mode, allowDuplicates)
        hasScheduledInLastLoop = true
        delay(delay.random().toLong())
    }

    private fun bindText(name: String) = text(name, "NONE") { manualSwap }.onChange { _, new ->
        normalizeBindName(new)
    }

    private fun resolveManualArmorType(key: Int): Int? = when (key) {
        keyCodeOf(helmetBind) -> 0
        keyCodeOf(chestplateBind) -> 1
        keyCodeOf(leggingsBind) -> 2
        keyCodeOf(bootsBind) -> 3
        else -> null
    }

    private fun keyCodeOf(bind: String): Int {
        val normalized = normalizeBindName(bind)
        if (normalized == "NONE") {
            return Keyboard.KEY_NONE
        }

        return Keyboard.getKeyIndex(normalized)
    }

    private fun normalizeBindName(bind: String): String {
        val normalized = bind.trim().uppercase().replace(" ", "")
        return if (normalized.isBlank()) "NONE" else normalized
    }

    private fun toArmorPriorityMode(selection: String) = when (selection) {
        "BestPercent" -> "Durability"
        "Defense" -> "Defense"
        "Enchantments" -> "Enchantments"
        else -> "Balanced"
    }

    private fun resetHighlight() {
        autoArmorCurrentSlot = -1
        autoArmorLastSlot = -1
    }
}
