/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.potion.Potion

@ModuleInfo(name = "AutoPot", category = ModuleCategory.COMBAT)
class AutoPot : Module() {
    private val healthValue = FloatValue("Health", 15F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 500, 500, 1000)
    private val throwTickValue = IntegerValue("ThrowTick", 3, 1, 10)
    private val selectValue = IntegerValue("SelectSlot", -1, -1, 9)

    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)
    private val regen = BoolValue("Regen", true)
    private val utility = BoolValue("Utility", true)
    private val notCombat = BoolValue("NotCombat", true)

    private var throwing = false
    private var throwTime = 0
    private var pot = -1

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (notCombat.get() && LiquidBounce.combatManager.inCombat) return
        if (!mc.thePlayer.onGround) return

        if (throwing) {
            throwTime++
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90F))
            if (throwTime == throwTickValue.get()) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(pot - 36))
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                pot = -1
            }
            if (throwTime >= (throwTickValue.get() * 2)) {
                throwTime = 0
                throwing = false
            }
            return
        }
        if (!InventoryUtils.INV_TIMER.hasTimePassed(delayValue.get().toLong())) return

        val enableSelect = selectValue.get() != -1
        val potion = if (enableSelect) {
            if (findSinglePotion(36 + selectValue.get())) {
                36 + selectValue.get()
            } else {
                -1
            }
        } else {
            findPotion(36, 45)
        }

        if (potion != -1) {
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90F))
            pot = potion
            throwing = true
            InventoryUtils.INV_TIMER.reset()
            return
        }

        if (openInventoryValue.get() && !enableSelect) {
            val invPotion = findPotion(9, 36)
            if (invPotion != -1) {
                val openInventory = mc.currentScreen !is GuiInventory && simulateInventory.get()
                if (InventoryUtils.hasSpaceHotbar()) {
                    if (openInventory) {
                        mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                    }

                    mc.playerController.windowClick(0, invPotion, 0, 1, mc.thePlayer)

                    if (openInventory) {
                        mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
                    }

                    return
                } else {
                    for (i in 36 until 45) {
                        val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                        if (stack == null || stack.item !is ItemPotion || !ItemPotion.isSplash(stack.itemDamage)) {
                            continue
                        }

                        if (openInventory) {
                            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                        }

                        mc.playerController.windowClick(0, invPotion, 0, 0, mc.thePlayer)
                        mc.playerController.windowClick(0, i, 0, 0, mc.thePlayer)

                        if (openInventory) {
                            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
                        }

                        break
                    }
                }
            }
        }
    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int {
        for (i in startSlot until endSlot) {
            if (findSinglePotion(i)) {
                return i
            }
        }
        return -1
    }

    private fun findSinglePotion(slot: Int): Boolean {
        val stack = mc.thePlayer.inventoryContainer.getSlot(slot).stack

        if (stack == null || stack.item !is ItemPotion || !ItemPotion.isSplash(stack.itemDamage)) {
            return false
        }

        val itemPotion = stack.item as ItemPotion

        if (mc.thePlayer.health <healthValue.get() && regen.get()) {
            for (potionEffect in itemPotion.getEffects(stack)) {
                if (potionEffect.potionID == Potion.heal.id) {
                    return true
                }
            }

            if (!mc.thePlayer.isPotionActive(Potion.regeneration)) {
                for (potionEffect in itemPotion.getEffects(stack)) {
                    if (potionEffect.potionID == Potion.regeneration.id) return true
                }
            }
        } else if (utility.get()) {
            for (potionEffect in itemPotion.getEffects(stack)) {
                if (InventoryUtils.isPositivePotionEffect(potionEffect.potionID) && !mc.thePlayer.isPotionActive(potionEffect.potionID)) {
                    return true
                }
            }
        }

        return false
    }
}