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
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import org.lwjgl.input.Keyboard

private val ARMOR_BIND_KEYS = mutableListOf<String>().apply {
    add("NONE")

    for (key in 1 until Keyboard.KEYBOARD_SIZE) {
        val keyName = Keyboard.getKeyName(key)

        if (keyName.isNullOrBlank() || keyName.equals("NONE", true) || keyName.equals("UNKNOWN", true)) {
            continue
        }

        if (keyName !in this) {
            add(keyName)
        }
    }
}.toTypedArray()

private data class ManualArmorRequest(val armorType: Int, val cycle: Boolean)

object AutoArmor : Module("AutoArmor", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {
    private val delay by intRange("Delay", 50..50, 0..1000)
    private val minItemAge by int("MinItemAge", 0, 0..2000)

    private val invOpen by +InventoryManager.invOpenValue
    private val simulateInventory by +InventoryManager.simulateInventoryValue

    private val postInventoryCloseDelay by +InventoryManager.postInventoryCloseDelayValue
    private val autoClose by +InventoryManager.autoCloseValue
    private val startDelay by +InventoryManager.startDelayValue
    private val closeDelay by +InventoryManager.closeDelayValue

    // When swapping armor pieces, it grabs the better one, drags and swaps it with equipped one.
    // This keeps armor coverage while preserving the old piece in the original slot.
    val smartSwap by boolean("SmartSwap", true)

    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val hotbar by boolean("Hotbar", true)

    // Sacrifices 1 tick speed for complete undetectability, needed to bypass Vulcan.
    private val delayedSlotSwitch by boolean("DelayedSlotSwitch", true) { hotbar }

    // Prevents AutoArmor from hotbar equipping while any screen is open.
    private val notInContainers by boolean("NotInContainers", false) { hotbar }

    private val manualSwap by boolean("ManualSwap", true)
    private val manualSwapMode by choices("ManualSwapMode", arrayOf("Best", "Cycle"), "Best") { manualSwap }
    private val helmetBind by bindChoice("HelmetBind")
    private val chestplateBind by bindChoice("ChestplateBind")
    private val leggingsBind by bindChoice("LeggingsBind")
    private val bootsBind by bindChoice("BootsBind")

    val highlightSlot by +InventoryManager.highlightSlotValue
    val backgroundColor by +InventoryManager.borderColor

    val borderStrength by +InventoryManager.borderStrength
    val borderColor by +InventoryManager.borderColor

    @Volatile
    private var pendingManualArmorRequest: ManualArmorRequest? = null

    val onKey = handler<KeyEvent> { event ->
        if (!manualSwap || event.key == Keyboard.KEY_NONE || event.key == keyBind) {
            return@handler
        }

        resolveManualArmorType(event.key)?.let {
            pendingManualArmorRequest = ManualArmorRequest(it, manualSwapMode == "Cycle")
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

        val candidate = selectManualCandidate(request, stacks) ?: return false
        val hasScheduledClick = equipArmorCandidate(request.armorType, candidate.first, candidate.second, stacks)

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

            // Check if the armor piece is in the hotbar.
            val hotbarIndex = index?.toHotbarIndex(stacks.size) ?: continue

            if (isTicked(index) || isTicked(armorType + 5))
                continue

            if (!stack.hasItemAgePassed(minItemAge))
                continue

            val armorPos = getArmorPosition(stack) - 1

            // Check if target armor slot isn't occupied.
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

                // Switch selected hotbar slot, right click to equip.
                sendPacket(C08PacketPlayerBlockPlacement(stack))

                // Instantly update inventory on client-side to prevent repetitive clicking because of ping.
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

    private fun selectManualCandidate(
        request: ManualArmorRequest,
        stacks: List<ItemStack?>,
    ): Pair<Int?, ItemStack>? {
        val candidates = ArmorFilter.getArmorCandidates(stacks, request.armorType)
            .filter { (_, stack) -> stack.hasItemAgePassed(minItemAge) }

        if (candidates.isEmpty()) {
            return null
        }

        if (!request.cycle) {
            return candidates.first()
        }

        val equippedStack = stacks.getOrNull(request.armorType + 5)
        val currentIndex = candidates.indexOfFirst { (_, stack) -> stack == equippedStack }

        return candidates[if (currentIndex == -1) 0 else (currentIndex + 1) % candidates.size]
    }

    private suspend fun equipArmorCandidate(
        armorType: Int,
        index: Int?,
        stack: ItemStack,
        stacks: List<ItemStack?>,
    ): Boolean {
        index ?: return false

        if (isTicked(index) || isTicked(armorType + 5))
            return false

        if (!stack.hasItemAgePassed(minItemAge))
            return false

        if (canBeRepairedWithOther(stack, stacks))
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

                    if (storageSlot != null) {
                        click(armorType + 5, 0, 0)
                        click(storageSlot, 0, 0)
                    } else {
                        click(armorType + 5, 0, 4)
                    }

                    click(index, 0, 1)
                }
            }
        }

        return true
    }

    private fun findStorageSlot(stacks: List<ItemStack?>, excludedSlot: Int): Int? =
        stacks.indices.firstOrNull { it >= 9 && it != excludedSlot && stacks[it] == null }

    private suspend fun shouldOperate(onlyHotbar: Boolean = false): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (!passedPostInventoryCloseDelay)
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            // It is impossible to equip armor when a container is open; only try to equip by right-clicking from hotbar (if NotInContainers is disabled).
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

        if (simulateInventory || invOpen)
            serverOpenInventory = true

        if (isFirstInventoryClick) {
            isFirstInventoryClick = false
            delay(startDelay.toLong())
        }

        clickNextTick(slot, button, mode, allowDuplicates)
        hasScheduledInLastLoop = true
        delay(delay.random().toLong())
    }

    private fun bindChoice(name: String) = choices(name, ARMOR_BIND_KEYS, "NONE") { manualSwap }

    private fun resolveManualArmorType(key: Int): Int? = when (key) {
        keyCodeOf(helmetBind) -> 0
        keyCodeOf(chestplateBind) -> 1
        keyCodeOf(leggingsBind) -> 2
        keyCodeOf(bootsBind) -> 3
        else -> null
    }

    private fun keyCodeOf(bind: String): Int {
        if (bind.equals("NONE", true)) {
            return Keyboard.KEY_NONE
        }

        return Keyboard.getKeyIndex(bind.uppercase())
    }

    private fun resetHighlight() {
        autoArmorCurrentSlot = -1
        autoArmorLastSlot = -1
    }
}
