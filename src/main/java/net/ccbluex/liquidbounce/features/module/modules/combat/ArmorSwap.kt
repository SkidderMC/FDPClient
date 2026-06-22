/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.hasScheduledInLastLoop
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.passedPostInventoryCloseDelay
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.durability
import net.ccbluex.liquidbounce.utils.timing.TickedActions.awaitTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clickNextTick
import net.ccbluex.liquidbounce.utils.timing.TickedActions.clearTicked
import net.ccbluex.liquidbounce.utils.timing.TickedActions.isTicked
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import kotlin.math.roundToLong

object ArmorSwap : Module("ArmorSwap", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {
    private enum class State {
        Idle,
        Opening,
        Processing,
        Organizing,
        Closing
    }

    private data class ArmorInfo(
        val equipped: ItemStack?,
        val threshold: Int,
        var bestValue: Long,
        var bestSlot: Int = -1
    )

    private val delayValue by int("Delay", 80, 1..500, suffix = "ms")
        .describe("Delay between armor swap actions.")
    private val swapAll by boolean("SwapAll", false)
        .describe("Use one threshold for all armor pieces.")
    private val percentage by int("Percentage", 25, 0..100, suffix = "%") { swapAll }
        .describe("Durability below which all armor is swapped.")
    private val helmetPct by int("Helmet", 25, 0..100, suffix = "%") { !swapAll }
        .describe("Durability below which the helmet is swapped.")
    private val chestPct by int("Chest", 25, 0..100, suffix = "%") { !swapAll }
        .describe("Durability below which the chestplate is swapped.")
    private val legsPct by int("Legs", 25, 0..100, suffix = "%") { !swapAll }
        .describe("Durability below which the leggings are swapped.")
    private val bootsPct by int("Boots", 25, 0..100, suffix = "%") { !swapAll }
        .describe("Durability below which the boots are swapped.")
    private val autoDrop by boolean("AutoDrop", false)
        .describe("Drop the old armor after swapping.")
    private val multiSwap by boolean("MultiSwap", false)
        .describe("Swap multiple armor pieces in one pass.")
    private val openInventory by boolean("OpenInventory", true)
        .describe("Open the inventory to perform the swap.")
    private val inventoryOrganizer by boolean("InventoryOrganizer", false)
        .describe("Move spare armor into fixed hotbar slots.")
    private val helmetSlot by int("HelmetSlot", 1, 1..9) { inventoryOrganizer }
        .describe("Hotbar slot used to store the helmet.")
    private val chestSlot by int("ChestSlot", 2, 1..9) { inventoryOrganizer }
        .describe("Hotbar slot used to store the chestplate.")
    private val legsSlot by int("LegsSlot", 3, 1..9) { inventoryOrganizer }
        .describe("Hotbar slot used to store the leggings.")
    private val bootsSlot by int("BootsSlot", 4, 1..9) { inventoryOrganizer }
        .describe("Hotbar slot used to store the boots.")

    private val startDelay by +InventoryManager.startDelayValue
    private val closeDelay by +InventoryManager.closeDelayValue
    private val noMove by +InventoryManager.noMoveValue
    private val noMoveAir by +InventoryManager.noMoveAirValue
    private val noMoveGround by +InventoryManager.noMoveGroundValue

    private val toReplace = BooleanArray(4)
    private var workState = State.Idle
    private var lastActionTime = 0L
    private var lastCloseTime = 0L
    private var statusMessage = ""
    private var statusTime = 0L

    override val tag: String?
        get() {
            val now = System.currentTimeMillis()
            if (statusMessage.isNotEmpty() && now - statusTime < 1500L) {
                return statusMessage
            }

            if (workState == State.Opening || workState == State.Processing || workState == State.Organizing) {
                return "Trocando set"
            }

            return if (multiSwap) "MultiSwap" else "${delayValue}ms"
        }

    override fun onDisable() {
        resetWorkState()
    }

    suspend fun runSwap(): Boolean {
        if (!handleEvents()) {
            if (workState != State.Idle) {
                resetWorkState()
            }
            return false
        }

        val now = System.currentTimeMillis()
        if (workState != State.Idle && now - lastActionTime < delayValue) {
            return true
        }

        return when (workState) {
            State.Idle -> prepareSwap(now)
            State.Opening -> openInventoryForSwap(now)
            State.Processing -> processSwap(now)
            State.Organizing -> organizeInventory(now)
            State.Closing -> closeInventoryAfterSwap(now)
        }
    }

    private suspend fun prepareSwap(now: Long): Boolean {
        if (now - lastCloseTime < 500L || !shouldOperate(requireOpenInventory = false)) {
            return false
        }

        val player = mc.thePlayer ?: return false
        val stacks = withContext(Dispatchers.Main) {
            player.openContainer.inventorySlots.map { it.stack }
        }

        val armorInfo = Array(4) { type ->
            val equipped = stacks.getOrNull(5 + type)
            val threshold = thresholdFor(type)
            val needsReplacement = equipped == null || durabilityPercent(equipped) <= threshold
            ArmorInfo(equipped, threshold, if (needsReplacement) Long.MIN_VALUE else getArmorValue(equipped))
        }

        var missingAnyPiece = false
        var hasAnyArmorInInventory = false
        var hasNewArmorInInventory = false

        for (type in 0..3) {
            val info = armorInfo[type]
            if (info.equipped == null) {
                missingAnyPiece = true
            }

            for (slot in 9..44) {
                val stack = stacks.getOrNull(slot) ?: continue
                val armor = stack.item as? ItemArmor ?: continue
                if (armor.armorType != type) {
                    continue
                }

                hasAnyArmorInInventory = true
                if (ArmorFilter.handleEvents() && !ArmorFilter.isArmorAllowed(stack)) {
                    continue
                }

                val durability = durabilityPercent(stack)
                if (durability > info.threshold) {
                    hasNewArmorInInventory = true
                }

                val value = getArmorValue(stack)
                val isBetter = value > info.bestValue ||
                    inventoryOrganizer && slot >= 36 && value == info.bestValue && info.bestSlot < 36

                if (isBetter) {
                    info.bestValue = value
                    info.bestSlot = slot
                }
            }
        }

        var canSwapSomething = false
        var needsArmor = false

        if (swapAll) {
            val anyPieceBad = armorInfo.any { it.equipped == null || durabilityPercent(it.equipped) <= it.threshold }
            if (anyPieceBad) {
                for (type in 0..3) {
                    toReplace[type] = armorInfo[type].bestSlot != -1
                    canSwapSomething = canSwapSomething || toReplace[type]
                }

                if (!canSwapSomething) {
                    needsArmor = true
                }
            } else {
                toReplace.fill(false)
            }
        } else {
            for (type in 0..3) {
                val info = armorInfo[type]
                val currentBad = info.equipped == null || durabilityPercent(info.equipped) <= info.threshold
                toReplace[type] = info.bestSlot != -1 && currentBad
                canSwapSomething = canSwapSomething || toReplace[type]

                if (currentBad) {
                    needsArmor = true
                }
            }
        }

        lastActionTime = now

        if (canSwapSomething) {
            workState = State.Opening
            setStatus("Trocando set", now)
            return true
        }

        if (needsArmor) {
            when {
                missingAnyPiece && !hasAnyArmorInInventory -> setStatus("Sem set", now)
                hasAnyArmorInInventory && !hasNewArmorInInventory -> setStatus("Sem set + novo", now)
            }
        }

        return false
    }

    private suspend fun openInventoryForSwap(now: Long): Boolean {
        if (!shouldOperate(requireOpenInventory = false)) {
            resetWorkState()
            return false
        }

        if (mc.currentScreen !is GuiInventory && openInventory) {
            serverOpenInventory = true
        }

        workState = State.Processing
        return true
    }

    private suspend fun processSwap(now: Long): Boolean {
        if (!shouldOperate()) {
            resetWorkState()
            return false
        }

        val player = mc.thePlayer ?: return false
        val stacks = withContext(Dispatchers.Main) {
            player.openContainer.inventorySlots.map { it.stack }
        }

        var swappedAny = false

        for (type in 0..3) {
            if (!toReplace[type]) {
                continue
            }

            val replacementSlot = findBestReplacementSlot(type, stacks)
            if (replacementSlot == -1) {
                toReplace[type] = false
                continue
            }

            val armorSlot = 5 + type
            if (isTicked(replacementSlot) || isTicked(armorSlot)) {
                continue
            }

            click(replacementSlot, 0, 0)
            click(armorSlot, 0, 0)
            click(replacementSlot, 0, 0, allowDuplicates = true)

            if (autoDrop) {
                click(replacementSlot, 1, 4, allowDuplicates = true)
            }

            swappedAny = true
            toReplace[type] = false

            if (!multiSwap) {
                break
            }
        }

        if (swappedAny) {
            lastActionTime = now
            delay(delayValue.toLong())
            awaitTicked()
        }

        if (toReplace.none { it }) {
            workState = if (inventoryOrganizer) State.Organizing else State.Closing
        }

        return true
    }

    private suspend fun organizeInventory(now: Long): Boolean {
        if (!shouldOperate()) {
            resetWorkState()
            return false
        }

        val player = mc.thePlayer ?: return false
        val stacks = withContext(Dispatchers.Main) {
            player.openContainer.inventorySlots.map { it.stack }
        }

        val targetSlots = intArrayOf(helmetSlot, chestSlot, legsSlot, bootsSlot)
            .map { 35 + it }

        var movedAny = false
        for (type in 0..3) {
            val targetSlot = targetSlots[type]
            if (targetSlot !in 36..44) {
                continue
            }

            val sourceSlot = findBestInRange(type, stacks, 9..35)
            if (sourceSlot == -1) {
                continue
            }

            if (isTicked(sourceSlot) || isTicked(targetSlot)) {
                continue
            }

            click(sourceSlot, 0, 0)
            click(targetSlot, 0, 0)
            click(sourceSlot, 0, 0, allowDuplicates = true)
            movedAny = true
        }

        if (movedAny) {
            delay(delayValue.toLong())
            awaitTicked()
        }

        lastActionTime = now
        workState = State.Closing
        return true
    }

    private suspend fun closeInventoryAfterSwap(now: Long): Boolean {
        delay(closeDelay.toLong())

        if (openInventory && mc.currentScreen !is GuiInventory && serverOpenInventory) {
            serverOpenInventory = false
        } else if (openInventory && mc.currentScreen is GuiInventory) {
            mc.thePlayer?.closeScreen()
        }

        setStatus("Set trocado", now)
        lastCloseTime = now
        lastActionTime = now
        resetWorkState(keepStatus = true)
        return true
    }

    private suspend fun shouldOperate(requireOpenInventory: Boolean = true): Boolean {
        if (!passedPostInventoryCloseDelay) {
            return false
        }

        if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true) {
            return false
        }

        if (mc.thePlayer?.openContainer?.windowId != 0) {
            return false
        }

        if (!openInventory && mc.currentScreen !is GuiInventory) {
            return false
        }

        if (requireOpenInventory && mc.currentScreen !is GuiInventory && !openInventory && !serverOpenInventory) {
            return false
        }

        return canClickInventory(closeWhenViolating = true)
    }

    private fun findBestReplacementSlot(armorType: Int, stacks: List<ItemStack?>): Int {
        if (inventoryOrganizer) {
            findBestInRange(armorType, stacks, 36..44).takeIf { it != -1 }?.let { return it }
            return findBestInRange(armorType, stacks, 9..35)
        }

        return findBestInRange(armorType, stacks, 9..44)
    }

    private fun findBestInRange(armorType: Int, stacks: List<ItemStack?>, range: IntRange): Int {
        var bestSlot = -1
        var bestValue = Long.MIN_VALUE

        for (slot in range) {
            val stack = stacks.getOrNull(slot) ?: continue
            val armor = stack.item as? ItemArmor ?: continue
            if (armor.armorType != armorType) {
                continue
            }
            if (ArmorFilter.handleEvents() && !ArmorFilter.isArmorAllowed(stack)) {
                continue
            }

            val value = getArmorValue(stack)
            if (value > bestValue) {
                bestValue = value
                bestSlot = slot
            }
        }

        return bestSlot
    }

    private fun thresholdFor(armorType: Int): Int {
        val threshold = if (swapAll) {
            percentage
        } else {
            when (armorType) {
                0 -> helmetPct
                1 -> chestPct
                2 -> legsPct
                else -> bootsPct
            }
        }

        return threshold.coerceIn(0, 100)
    }

    private fun durabilityPercent(stack: ItemStack?): Int {
        if (stack == null || stack.item !is ItemArmor) {
            return 0
        }

        val maxDamage = stack.maxDamage
        if (maxDamage <= 0) {
            return 100
        }

        return (stack.durability.coerceIn(0, maxDamage) * 100) / maxDamage
    }

    private fun getArmorValue(stack: ItemStack?): Long {
        val armor = stack?.item as? ItemArmor ?: return -1L
        if (ArmorFilter.handleEvents()) {
            return (ArmorFilter.scoreArmorStack(stack) * 1000.0).roundToLong()
        }

        val armorValue = armor.armorMaterial.getDamageReductionAmount(armor.armorType)
        return armorValue.toLong() * 1000L + durabilityPercent(stack)
    }

    private suspend fun click(slot: Int, button: Int, mode: Int, allowDuplicates: Boolean = false) {
        if (mc.currentScreen !is GuiInventory && openInventory) {
            serverOpenInventory = true
        }

        if (isFirstInventoryClick) {
            isFirstInventoryClick = false
            delay(startDelay.toLong())
        }

        clickNextTick(slot, button, mode, allowDuplicates)
        hasScheduledInLastLoop = true
    }

    private fun setStatus(message: String, now: Long = System.currentTimeMillis()) {
        statusMessage = message
        statusTime = now
    }

    private fun resetWorkState(keepStatus: Boolean = false) {
        workState = State.Idle
        toReplace.fill(false)
        clearTicked()

        if (!keepStatus) {
            statusMessage = ""
            statusTime = 0L
        }
    }
}
