/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.ResourceLocation
import kotlin.random.Random

@ModuleInfo(name = "Stealer", category = ModuleCategory.WORLD)
object Stealer : Module() {
    /**
     * OPTIONS
     */

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 200, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) {
                set(i)
            }

            nextDelay = TimeUtils.randomDelay(minDelayValue.get(), get())
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 150, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue) {
                set(i)
            }

            nextDelay = TimeUtils.randomDelay(get(), maxDelayValue.get())
        }
    }

    private val chestValue = IntegerValue("ChestOpenDelay", 300, 0, 1000)
    private val takeRandomizedValue = BoolValue("TakeRandomized", false)
    private val onlyItemsValue = BoolValue("OnlyItems", false)
    private val instantValue = BoolValue("Instant", false)
    private val noDuplicateValue = BoolValue("NoDuplicateNonStackable", false)
    private val noCompassValue = BoolValue("NoCompass", false)
    private val autoCloseValue = BoolValue("AutoClose", true)
    val silentValue = BoolValue("Silent", true)
    val silentTitleValue = BoolValue("SilentTitle", true)

    private val autoCloseMaxDelayValue: IntegerValue = object : IntegerValue("AutoCloseMaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMinDelayValue.get()
            if (i > newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), this.get())
        }
    }.displayable { autoCloseValue.get() } as IntegerValue

    private val autoCloseMinDelayValue: IntegerValue = object : IntegerValue("AutoCloseMinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMaxDelayValue.get()
            if (i < newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(this.get(), autoCloseMaxDelayValue.get())
        }
    }.displayable { autoCloseValue.get() } as IntegerValue

    private val closeOnFullValue = BoolValue("CloseOnFull", true).displayable { autoCloseValue.get() }
    val chestTitleValue = BoolValue("ChestTitle", false)

    /**
     * VALUES
     */
    private val delayTimer = MSTimer()
    private val chestTimer = MSTimer()
    private var nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

    private val autoCloseTimer = MSTimer()
    private var nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

    private var contentReceived = 0

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!chestTimer.hasTimePassed(chestValue.get().toLong())) {
            return
        }

        val screen = mc.currentScreen

        if (screen !is GuiChest || !delayTimer.hasTimePassed(nextDelay)) {
            autoCloseTimer.reset()
            return
        }

        // No Compass
        if (noCompassValue.get() && mc.thePlayer.inventory.getCurrentItem()?.item?.unlocalizedName == "item.compass") {
            return
        }

        // Chest title
        if (chestTitleValue.get() && (screen.lowerChestInventory == null || !screen.lowerChestInventory.name.contains(ItemStack(Item.itemRegistry.getObject(ResourceLocation("minecraft:chest"))).displayName))) {
            return
        }

        // inventory cleaner
        val invManager = FDPClient.moduleManager[InvManager::class.java]!!

        // check if it's empty?
        if (!isEmpty(screen) && !(closeOnFullValue.get() && fullInventory)) {
            autoCloseTimer.reset()

            // Randomized
            if (takeRandomizedValue.get()) {
                do {
                    val items = mutableListOf<Slot>()

                    for (slotIndex in 0 until screen.inventoryRows * 9) {
                        val slot = screen.inventorySlots.inventorySlots[slotIndex]

                        if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }
                                        .map { it.item!! }
                                        .contains(slot.stack.item)) && (!invManager.state || invManager.isUseful(
                                        slot.stack,
                                        -1
                                ))
                        )
                            items.add(slot)
                    }

                    val randomSlot = Random.nextInt(items.size)
                    val slot = items[randomSlot]

                    move(screen, slot)
                } while (delayTimer.hasTimePassed(nextDelay) && items.isNotEmpty())
                return
            }

            // Non randomized
            for (slotIndex in 0 until screen.inventoryRows * 9) {
                val slot = screen.inventorySlots.inventorySlots[slotIndex]

                if (delayTimer.hasTimePassed(nextDelay) && slot.stack != null &&
                        (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!invManager.state || invManager.isUseful(slot.stack, -1))) {
                    move(screen, slot)
                }
            }
        } else if (autoCloseValue.get() && screen.inventorySlots.windowId == contentReceived && autoCloseTimer.hasTimePassed(nextCloseDelay)) {
            mc.thePlayer.closeScreen()
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (instantValue.get()) {
            if (mc.currentScreen is GuiChest) {
                val chest = mc.currentScreen as GuiChest
                val rows = chest.inventoryRows * 9
                for (i in 0 until rows) {
                    val slot = chest.inventorySlots.getSlot(i)
                    if (slot.hasStack) {
                        mc.thePlayer.sendQueue.addToSendQueue(
                                C0EPacketClickWindow(
                                        chest.inventorySlots.windowId,
                                        i,
                                        0,
                                        1,
                                        slot.stack,
                                        1.toShort()
                                )
                        )
                    }
                }
                mc.thePlayer.closeScreen()
            }
        }
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S30PacketWindowItems) {
            contentReceived = packet.func_148911_c()
        }

        if (packet is S2DPacketOpenWindow) {
            chestTimer.reset()
        }
    }

    private fun move(screen: GuiChest, slot: Slot) {
        screen.handleMouseClick(slot, slot.slotNumber, 0, 1)
        delayTimer.reset()
        nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    private fun isEmpty(chest: GuiChest): Boolean {
        val invManager = FDPClient.moduleManager[InvManager::class.java]!!

        for (i in 0 until chest.inventoryRows * 9) {
            val slot = chest.inventorySlots.inventorySlots[i]

            if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!invManager.state || invManager.isUseful(slot.stack, -1))) {
                return false
            }
        }

        return true
    }

    private val fullInventory: Boolean
        get() = mc.thePlayer.inventory.mainInventory.none { it == null }
}
