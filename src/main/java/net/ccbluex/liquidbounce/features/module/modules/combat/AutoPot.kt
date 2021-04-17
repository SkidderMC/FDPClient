/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
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

@ModuleInfo(name = "AutoPot", description = "Automatically throws healing potions.", category = ModuleCategory.COMBAT)
class AutoPot : Module() {
    private val healthValue = FloatValue("Health", 15F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 500, 500, 1000)

    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)
    private val regen = BoolValue("Regen", true)
    private val utility = BoolValue("Utility", true)

    private val timer = MSTimer()
    private var throwing=false
    private var pot=-1

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(!mc.thePlayer.onGround) return

        if(throwing){
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw,90F))
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(pot-36))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            throwing=false
            pot=-1
        }
        if(!timer.hasTimePassed(delayValue.get().toLong())) return

        val potion=findPotion(36,45)
        if(potion!=-1){
            RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw,90F))
            pot=potion
            throwing=true
            timer.reset()
            return
        }

        if(openInventoryValue.get()){
            val invPotion=findPotion(9,36)
            if (invPotion != -1) {
                val openInventory = mc.currentScreen !is GuiInventory && simulateInventory.get()
                if(InventoryUtils.hasSpaceHotbar()) {
                    if (openInventory)
                        mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))

                    mc.playerController.windowClick(0, invPotion, 0, 1, mc.thePlayer)

                    if (openInventory)
                        mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

                    return
                }else{
                    for (i in 36 until 45) {
                        val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                        if (stack == null || stack.item !is ItemPotion || !ItemPotion.isSplash(stack.itemDamage))
                            continue

                        if (openInventory)
                            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))

                        mc.playerController.windowClick(0, invPotion, 0, 0, mc.thePlayer)
                        mc.playerController.windowClick(0, i, 0, 0, mc.thePlayer)

                        if (openInventory)
                            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

                        break
                    }
                }
            }
        }
    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int {
        for (i in startSlot until endSlot) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack

            if (stack == null || stack.item !is ItemPotion || !ItemPotion.isSplash(stack.itemDamage))
                continue

            val itemPotion = stack.item as ItemPotion

            if(mc.thePlayer.health<healthValue.get()&&regen.get()) {
                for (potionEffect in itemPotion.getEffects(stack))
                    if (potionEffect.potionID == Potion.heal.id)
                        return i

                if (!mc.thePlayer.isPotionActive(Potion.regeneration))
                    for (potionEffect in itemPotion.getEffects(stack))
                        if (potionEffect.potionID == Potion.regeneration.id) return i
            }else if(utility.get()){
                for (potionEffect in itemPotion.getEffects(stack)){
                    if(isUsefulPotion(potionEffect.potionID)) return i
                }
            }
        }
        return -1
    }

    private fun isUsefulPotion(id: Int):Boolean{
        if(id==Potion.regeneration.id||id==Potion.heal.id||id==Potion.poison.id
            ||id==Potion.blindness.id||id==Potion.harm.id||id==Potion.wither.id
            ||id==Potion.digSlowdown.id||id==Potion.moveSlowdown.id){
            return false
        }
        return !mc.thePlayer.isPotionActive(id)
    }
}