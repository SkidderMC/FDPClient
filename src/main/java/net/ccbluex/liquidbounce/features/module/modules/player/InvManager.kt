/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.access.IItemStack
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.item.ArmorPiece
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import java.util.stream.Collectors
import java.util.stream.IntStream

@ModuleInfo(name = "InvManager", category = ModuleCategory.PLAYER)
class InvManager : Module() {

    /**
     * OPTIONS
     */

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 600, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minCPS = minDelayValue.get()
            if (minCPS > newValue) set(minCPS)
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 400, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) set(maxDelay)
        }
    }

    private val invOpenValue = BoolValue("InvOpen", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)
    private val simulateDelayValue = IntegerValue("SimulateInventoryDelay", 0, 0, 1000).displayable { simulateInventory.get() }
    private val noMoveValue = BoolValue("NoMove", false)
    private val hotbarValue = BoolValue("Hotbar", true)
    private val randomSlotValue = BoolValue("RandomSlot", false)
    private val sortValue = BoolValue("Sort", true)
    private val throwValue = BoolValue("ThrowGarbage", true)
    private val armorValue = BoolValue("Armor", true)
    private val noCombatValue = BoolValue("NoCombat", false)
    private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000)
    private val onlySwordDamage = BoolValue("OnlySwordWeapon", true)
    private val swingValue = BoolValue("Swing", true)
    private val nbtGoalValue = ListValue("NBTGoal", ItemUtils.EnumNBTPriorityType.values().map { it.toString() }.toTypedArray(), "NONE")
    private val nbtItemNotGarbage = BoolValue("NBTItemNotGarbage", true).displayable { !nbtGoalValue.equals("NONE") }
    private val nbtArmorPriority = FloatValue("NBTArmorPriority", 0f, 0f, 5f).displayable { !nbtGoalValue.equals("NONE") }
    private val nbtWeaponPriority = FloatValue("NBTWeaponPriority", 0f, 0f, 5f).displayable { !nbtGoalValue.equals("NONE") }
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)
    private val onlyPositivePotionValue = BoolValue("OnlyPositivePotion", false)
//    private val ignoreDurabilityUnder = FloatValue("IgnoreDurabilityUnder", 0.3f, 0f, 1f)

    private val items = arrayOf("None", "Ignore", "Sword", "Bow", "Pickaxe", "Axe", "Food", "Block", "Water", "Gapple", "Pearl", "Potion")
    private val sortSlot1Value = ListValue("SortSlot-1", items, "Sword").displayable { sortValue.get() }
    private val sortSlot2Value = ListValue("SortSlot-2", items, "Gapple").displayable { sortValue.get() }
    private val sortSlot3Value = ListValue("SortSlot-3", items, "Potion").displayable { sortValue.get() }
    private val sortSlot4Value = ListValue("SortSlot-4", items, "Pickaxe").displayable { sortValue.get() }
    private val sortSlot5Value = ListValue("SortSlot-5", items, "Axe").displayable { sortValue.get() }
    private val sortSlot6Value = ListValue("SortSlot-6", items, "None").displayable { sortValue.get() }
    private val sortSlot7Value = ListValue("SortSlot-7", items, "Block").displayable { sortValue.get() }
    private val sortSlot8Value = ListValue("SortSlot-8", items, "Pearl").displayable { sortValue.get() }
    private val sortSlot9Value = ListValue("SortSlot-9", items, "Food").displayable { sortValue.get() }

    private val openInventory: Boolean
        get() = mc.currentScreen !is GuiInventory && simulateInventory.get()

    /**
     * means of simulating inventory
     */
    private var invOpened = false
        set(value) {
            if (value != field) {
                if (value) {
                    InventoryUtils.openPacket()
                } else {
                    InventoryUtils.closePacket()
                }
            }
            field = value
        }

    private val goal: ItemUtils.EnumNBTPriorityType
        get() = ItemUtils.EnumNBTPriorityType.valueOf(nbtGoalValue.get())

    private var delay = 0L
    private val simDelayTimer = MSTimer()

    override fun onDisable() {
        invOpened = false
    }

    private fun checkOpen(): Boolean {
        if (!invOpened && openInventory) {
            invOpened = true
            simDelayTimer.reset()
            return true
        }
        return !simDelayTimer.hasTimePassed(simulateDelayValue.get().toLong())
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (noMoveValue.get() && MovementUtils.isMoving() ||
            mc.thePlayer.openContainer != null && mc.thePlayer.openContainer.windowId != 0 ||
            (LiquidBounce.combatManager.inCombat && noCombatValue.get())) {
            if(InventoryUtils.CLICK_TIMER.hasTimePassed(simulateDelayValue.get().toLong())) {
                invOpened = false
            }
            return
        }

        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) || (mc.currentScreen !is GuiInventory && invOpenValue.get())) {
            return
        }

        if (armorValue.get()) {
            // Find best armor
            val bestArmor = findBestArmor()

            // Swap armor
            for (i in 0..3) {
                val armorPiece = bestArmor[i] ?: continue
                val armorSlot = 3 - i
                val oldArmor: ItemStack? = mc.thePlayer.inventory.armorItemInSlot(armorSlot)
                if (oldArmor == null || oldArmor.item !is ItemArmor || ItemUtils.compareArmor(ArmorPiece(oldArmor, -1), armorPiece, nbtArmorPriority.get(), goal) < 0) {
                    if (oldArmor != null && move(8 - armorSlot, true)) {
                        return
                    }
                    if (mc.thePlayer.inventory.armorItemInSlot(armorSlot) == null && move(armorPiece.slot, false)) {
                        return
                    }
                }
            }
        }

        if (sortValue.get()) {
            for (index in 0..8) {
                val bestItem = findBetterItem(index, mc.thePlayer.inventory.getStackInSlot(index)) ?: continue

                if (bestItem != index) {
                    if (checkOpen()) {
                        return
                    }

                    mc.playerController.windowClick(0, if (bestItem < 9) bestItem + 36 else bestItem, index, 2, mc.thePlayer)

                    delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                    return
                }
            }
        }

        if (throwValue.get()) {
            val garbageItems = items(5, if (hotbarValue.get()) 45 else 36)
                .filter { !isUseful(it.value, it.key) }
                .keys

            val garbageItem = if(garbageItems.isNotEmpty()) {
                if(randomSlotValue.get()) {
                    // pick random one
                    garbageItems.toList()[RandomUtils.nextInt(0, garbageItems.size)]
                } else {
                    garbageItems.first()
                }
            } else {
                null
            }
            if (garbageItem != null) {
                // Drop all useless items
                if (checkOpen()) {
                    return
                }
		
		if(swingValue.get()) mc.thePlayer.swingItem()

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, garbageItem, 1, 4, mc.thePlayer)

                delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

                return
            }
        }

        if(InventoryUtils.CLICK_TIMER.hasTimePassed(simulateDelayValue.get().toLong())) {
            invOpened = false
        }
    }

    /**
     * Checks if the item is useful
     *
     * @param slot Slot id of the item. If the item isn't in the inventory -1
     * @return Returns true when the item is useful
     */
    fun isUseful(itemStack: ItemStack, slot: Int): Boolean {
        return try {
            val item = itemStack.item

            if (item is ItemSword || (item is ItemTool && !onlySwordDamage.get())) {

                val damage = (itemStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) + ItemUtils.getWeaponEnchantFactor(itemStack, nbtWeaponPriority.get(), goal)

                items(0, 45).none { (_, stack) ->
		            if (stack != itemStack && stack.javaClass == itemStack.javaClass) {
			            val dmg = (stack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) + ItemUtils.getWeaponEnchantFactor(stack, nbtWeaponPriority.get(), goal)
			            if (damage == dmg) {
				            val currDamage = item.getDamage(itemStack)
				            currDamage >= stack.item.getDamage(stack)
			            } else damage < dmg
		            } else {false}
                }
            } else if (item is ItemBow) {
                val currPower = ItemUtils.getEnchantment(itemStack, Enchantment.power)

                /*items().none { (_, stack) ->
                    itemStack != stack && stack.item is ItemBow &&
                            currPower <= ItemUtils.getEnchantment(stack, Enchantment.power)
                }*/
                items().none { (_, stack) ->
		            if (itemStack != stack && stack.item is ItemBow) {
			            val power = ItemUtils.getEnchantment(stack, Enchantment.power)

			            if (currPower == power) {
				            val currDamage = item.getDamage(itemStack)
				            currDamage >= stack.item.getDamage(stack)
			            } else currPower < power
		            } else {false}
                }
            } else if (item is ItemArmor) {
                val currArmor = ArmorPiece(itemStack, slot)

                /*items().none { (slot, stack) ->
                    if (stack != itemStack && stack.item is ItemArmor) {
                        val armor = ArmorPiece(stack, slot)

                        if (armor.armorType != currArmor.armorType) {
                            false
                        } else {
                            ItemUtils.compareArmor(currArmor, armor, nbtArmorPriority.get(), goal) <= 0
                        }
                    } else {
                        false
                    }
                }*/
                items().none { (slot, stack) ->
                    if (stack != itemStack && stack.item is ItemArmor) {
                        val armor = ArmorPiece(stack, slot)

                        if (armor.armorType != currArmor.armorType) {false} 
                        else {
				            val currDamage = item.getDamage(itemStack)
				            val result = ItemUtils.compareArmor(currArmor, armor, nbtArmorPriority.get(), goal)
                        	if (result == 0)
			    		        currDamage >= stack.item.getDamage(stack)
				            else result < 0
                        }
                    } else {false}
                }
            } else if (item is ItemFlintAndSteel) {
		        val currDamage = item.getDamage(itemStack);
                items().none { (_, stack) ->
                    itemStack != stack && stack.item is ItemFlintAndSteel && currDamage >= stack.item.getDamage(stack)
                }
            } else if (itemStack.unlocalizedName == "item.compass") {
                items(0, 45).none { (_, stack) -> itemStack != stack && stack.unlocalizedName == "item.compass" }
            } else {
                (nbtItemNotGarbage.get() && ItemUtils.hasNBTGoal(itemStack, goal)) ||
                        item is ItemFood || itemStack.unlocalizedName == "item.arrow" ||
                        (item is ItemBlock && !InventoryUtils.isBlockListBlock(item)) ||
                        item is ItemBed || (item is ItemPotion && (!onlyPositivePotionValue.get() || InventoryUtils.isPositivePotion(item, itemStack))) ||
                        item is ItemEnderPearl || item is ItemBucket || ignoreVehiclesValue.get() && (item is ItemBoat || item is ItemMinecart)
            }
        } catch (ex: Exception) {
            ClientUtils.logError("(InvManager) Failed to check item: ${itemStack.unlocalizedName}.", ex)
            true
        }
    }

    private fun findBestArmor(): Array<ArmorPiece?> {
        val armorPieces = IntStream.range(0, 36)
            .filter { i: Int ->
                val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
                (itemStack != null && itemStack.item is ItemArmor &&
                        (i < 9 || System.currentTimeMillis() - (itemStack as IItemStack).itemDelay >= itemDelayValue.get()))
            }
            .mapToObj { i: Int -> ArmorPiece(mc.thePlayer.inventory.getStackInSlot(i), i) }
            .collect(Collectors.groupingBy { obj: ArmorPiece -> obj.armorType })

        val bestArmor = arrayOfNulls<ArmorPiece>(4)
        for ((key, value) in armorPieces) {
            bestArmor[key!!] = value.also { it.sortWith { armorPiece, armorPiece2 -> ItemUtils.compareArmor(armorPiece, armorPiece2, nbtArmorPriority.get(), goal) } }.lastOrNull()
        }

        return bestArmor
    }

    private fun findBetterItem(targetSlot: Int, slotStack: ItemStack?): Int? {
        val type = type(targetSlot)

        when (type.lowercase()) {
            "sword", "pickaxe", "axe" -> {
                val currentType: Class<out Item> = when {
                    type.equals("Sword", ignoreCase = true) -> ItemSword::class.java
                    type.equals("Pickaxe", ignoreCase = true) -> ItemPickaxe::class.java
                    type.equals("Axe", ignoreCase = true) -> ItemAxe::class.java
                    else -> return null
                }

                var bestWeapon = if (slotStack?.item?.javaClass == currentType) {
                    targetSlot
                } else {
                    -1
                }

                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack?.item?.javaClass == currentType && !type(index).equals(type, ignoreCase = true) && (!onlySwordDamage.get() || type.equals("Sword", ignoreCase = true)) ) {
                        if (bestWeapon == -1) {
                            bestWeapon = index
                        } else {
                            val currDamage = (itemStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) + ItemUtils.getWeaponEnchantFactor(itemStack, nbtWeaponPriority.get(), goal)

                            val bestStack = mc.thePlayer.inventory.getStackInSlot(bestWeapon) ?: return@forEachIndexed
                            val bestDamage = (bestStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount ?: 0.0) + ItemUtils.getWeaponEnchantFactor(bestStack, nbtWeaponPriority.get(), goal)

                            if (bestDamage < currDamage) {
                                bestWeapon = index
                            }
                        }
                    }
                }

                return if (bestWeapon != -1 || bestWeapon == targetSlot) bestWeapon else null
            }

            "bow" -> {
                var bestBow = if (slotStack?.item is ItemBow) targetSlot else -1
                var bestPower = if (bestBow != -1) {
                    ItemUtils.getEnchantment(slotStack!!, Enchantment.power)
                } else {
                    0
                }

                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack?.item is ItemBow && !type(index).equals(type, ignoreCase = true)) {
                        if (bestBow == -1) {
                            bestBow = index
                        } else {
                            val power = ItemUtils.getEnchantment(itemStack, Enchantment.power)

                            if (ItemUtils.getEnchantment(itemStack, Enchantment.power) > bestPower) {
                                bestBow = index
                                bestPower = power
                            }
                        }
                    }
                }

                return if (bestBow != -1) bestBow else null
            }

            "food" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemFood && item !is ItemAppleGold && !type(index).equals("Food", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemFood

                        return if (replaceCurr) index else null
                    }
                }
            }

            "block" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemBlock && !InventoryUtils.BLOCK_BLACKLIST.contains(item.block) &&
                        !type(index).equals("Block", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemBlock

                        return if (replaceCurr) index else null
                    }
                }
            }

            "water" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemBucket && item.isFull == Blocks.flowing_water && !type(index).equals("Water", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemBucket || (slotStack.item as ItemBucket).isFull != Blocks.flowing_water

                        return if (replaceCurr) index else null
                    }
                }
            }

            "gapple" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemAppleGold && !type(index).equals("Gapple", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemAppleGold

                        return if (replaceCurr) index else null
                    }
                }
            }

            "pearl" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemEnderPearl && !type(index).equals("Pearl", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemEnderPearl

                        return if (replaceCurr) index else null
                    }
                }
            }

            "potion" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if ((item is ItemPotion && ItemPotion.isSplash(stack.itemDamage)) &&
                        !type(index).equals("Potion", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemPotion || !ItemPotion.isSplash(slotStack.itemDamage)

                        return if (replaceCurr) index else null
                    }
                }
            }
        }

        return null
    }

    /**
     * Get items in inventory
     */
    private fun items(start: Int = 0, end: Int = 45): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in end - 1 downTo start) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
            itemStack.item ?: continue

            if (i in 36..44 && type(i).equals("Ignore", ignoreCase = true)) {
                continue
            }

            if (System.currentTimeMillis() - (itemStack as IItemStack).itemDelay >= itemDelayValue.get()) {
                items[i] = itemStack
            }
        }

        return items
    }

    /**
     * Shift+Left clicks the specified item
     *
     * @param item        Slot of the item to click
     * @param isArmorSlot
     * @return True if it is unable to move the item
     */
    private fun move(item: Int, isArmorSlot: Boolean): Boolean {
        if (item == -1) {
            return false
        } else if (!isArmorSlot && item < 9 && hotbarValue.get() && mc.currentScreen !is GuiInventory) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(item))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(item).stack))
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            return true
        } else {
            if (checkOpen()) {
                return true // make sure to return
            }
            if (throwValue.get() && isArmorSlot) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, item, 0, 4, mc.thePlayer)
            } else {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, if (isArmorSlot) item else if (item < 9) item + 36 else item, 0, 1, mc.thePlayer)
            }
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            return true
        }
    }

    /**
     * Get type of [targetSlot]
     */
    private fun type(targetSlot: Int) = when (targetSlot) {
        0 -> sortSlot1Value.get()
        1 -> sortSlot2Value.get()
        2 -> sortSlot3Value.get()
        3 -> sortSlot4Value.get()
        4 -> sortSlot5Value.get()
        5 -> sortSlot6Value.get()
        6 -> sortSlot7Value.get()
        7 -> sortSlot8Value.get()
        8 -> sortSlot9Value.get()
        else -> ""
    }
}
