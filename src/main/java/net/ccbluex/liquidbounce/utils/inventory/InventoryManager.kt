/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.inventory

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor
import net.ccbluex.liquidbounce.features.module.modules.other.ChestStealer
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.timeSinceClosedInventory
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.*

object InventoryManager : MinecraftInstance() {

	// Shared no move click values
	val noMoveValue = BoolValue("NoMoveClicks", false)
	val noMoveAirValue = BoolValue("NoClicksInAir", false) { noMoveValue.get() }
	val noMoveGroundValue = BoolValue("NoClicksOnGround", true) { noMoveValue.get() }

	// Shared values between AutoArmor and InventoryCleaner
	val invOpenValue = BoolValue("InvOpen", false)
	val simulateInventoryValue = BoolValue("SimulateInventory", true) { !invOpenValue.get() }
	val autoCloseValue = BoolValue("AutoClose", false) { invOpenValue.get() }

	val postInventoryCloseDelayValue = IntegerValue("PostInventoryCloseDelay", 0, 0..500)
	val startDelayValue = IntegerValue("StartDelay", 0, 0..500) { invOpenValue.get() || simulateInventoryValue.get() }
	val closeDelayValue = IntegerValue("CloseDelay", 0, 0..500) {
		if (invOpenValue.get()) autoCloseValue.get() else simulateInventoryValue.get()
	}

	// Shared highlight slot values
	val highlightSlotValue = BoolValue("Highlight-Slot", false, subjective = true)

	// Background and border values
	val backgroundRedValue = IntegerValue("Background-R", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val backgroundGreenValue = IntegerValue("Background-G", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val backgroundBlueValue = IntegerValue("Background-B", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val backgroundAlphaValue = IntegerValue("Background-Alpha", 128, 0..255, subjective = true) { highlightSlotValue.get() }

	val borderStrength = IntegerValue("Border-Strength", 3, 1..5, subjective = true) { highlightSlotValue.get() }
	val borderRed = IntegerValue("Border-R", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val borderGreen = IntegerValue("Border-G", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val borderBlue = IntegerValue("Border-B", 128, 0..255, subjective = true) { highlightSlotValue.get() }
	val borderAlpha = IntegerValue("Border-Alpha", 255, 0..255, subjective = true) { highlightSlotValue.get() }

	val undetectableValue = BoolValue("Undetectable", false)
	private val inventoryWorker = CoroutineScope(Dispatchers.Default + SupervisorJob())

	var hasScheduledInLastLoop = false
		set(value) {
			if (value) canCloseInventory = true
			field = value
		}

	private var canCloseInventory = false

	// Variables for slot highlighting
	var chestStealerCurrentSlot = -1
	var chestStealerLastSlot = -1
	var invCleanerCurrentSlot = -1
	var invCleanerLastSlot = -1
	var autoArmorCurrentSlot = -1
	var autoArmorLastSlot = -1

	val passedPostInventoryCloseDelay
		get() = System.currentTimeMillis() - timeSinceClosedInventory >= postInventoryCloseDelayValue.get()

	private suspend fun manageInventory() {
		while (inventoryWorker.isActive) {
			try {
				ChestStealer.stealFromChest()
				AutoArmor.equipFromHotbar()

				if (!canClickInventory() || (invOpenValue.get() && mc.currentScreen !is GuiInventory)) {
					delay(50)
					continue
				}

				canCloseInventory = false
				AutoArmor.equipFromInventory()
				InventoryCleaner.repairEquipment()
				InventoryCleaner.mergeStacks()
				InventoryCleaner.sortHotbar()
				InventoryCleaner.dropGarbage()

				val action = closingAction
				if (action == null) {
					delay(50)
					continue
				}

				delay(closeDelayValue.get().toLong())
				if (!hasScheduledInLastLoop) {
					action.invoke()
				}
			} catch (e: Exception) {
				displayChatMessage("§cReworked coroutine inventory management ran into an issue! Please report this: ${e.message ?: e.cause}")
				e.printStackTrace()
			}
		}
	}

	private val closingAction
		get() = when {
			!canCloseInventory -> null
			mc.thePlayer?.openContainer?.windowId != 0 -> null
			mc.currentScreen is GuiInventory && invOpenValue.get() && autoCloseValue.get() -> ({ mc.thePlayer?.closeScreen() })
			mc.currentScreen !is GuiInventory && simulateInventoryValue.get() && serverOpenInventory -> ({ serverOpenInventory = false })
			else -> null
		}

	fun canClickInventory(closeWhenViolating: Boolean = false): Boolean {
		return if (noMoveValue.get() && mc.thePlayer?.isMoving == true && if (serverOnGround) noMoveGroundValue.get() else noMoveAirValue.get()) {
			if (closeWhenViolating) serverOpenInventory = false
			false
		} else true
	}

	fun startCoroutine() = inventoryWorker.launch {
		manageInventory()
	}
}