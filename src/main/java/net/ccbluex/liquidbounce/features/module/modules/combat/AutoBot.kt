package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.init.Items
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo("AutoBot", category = ModuleCategory.COMBAT)
object AutoBot : Module() {

    private val autoSoupValue = BoolValue("AutoSoup", true)
    private val autoPotValue = BoolValue("AutoPot", true)
    private val autoBowValue = BoolValue("AutoBow", true)
    private val antiFireBallValue = BoolValue("AntiFireBall", true)

    // Auto Soup
    private val autoSoupHealthValue = FloatValue("Health", 15f, 0f, 20f).displayable { autoSoupValue.get() }
    private val autoSoupDelayValue = IntegerValue("Delay", 150, 0, 500).displayable { autoSoupValue.get() }
    private val autoSoupOpenInventoryValue = BoolValue("OpenInv", false).displayable { autoSoupValue.get() }
    private val autoSoupSimulateInventoryValue = BoolValue("SimulateInventory", true).displayable { autoSoupValue.get() }
    private val autoSoupBowlValue = ListValue("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop").displayable { autoSoupValue.get() }
    private val autoSoupTimer = MSTimer()

    // Auto Pot
    private val autoPotThrowMode = ListValue("AutoPot-ThrowMode", arrayOf("Up", "Forward", "Down", "Custom"), "Up").displayable { autoPotValue.get() }
    private val autoPotHealthValue = FloatValue("AutoPot-Health", 15F, 1F, 20F).displayable { autoPotValue.get() }
    private val autoPotDelayValue = IntegerValue("AutoPot-Delay", 500, 500, 1000).displayable { autoPotValue.get() }
    private val autoPotThrowTickValue = IntegerValue("AutoPot-ThrowTick", 3, 1, 10).displayable { autoPotValue.get() }
    private val autoPotSelectValue = IntegerValue("AutoPot-SelectSlot", -1, -1, 9).displayable { autoPotValue.get() }
    private val autoPotGroundDistanceValue = FloatValue("AutoPot-GroundDistance", 2F, 0F, 4F).displayable { autoPotValue.get() && !autoPotOnGround.get() }
    private val autoPotThrowAngleOption = IntegerValue("AutoPot-ThrowAngle", -45, -90, 90).displayable { autoPotThrowMode.equals("Custom") && autoPotValue.get() }
    private val autoPotOpenInventoryValue = BoolValue("AutoPot-OpenInv", false).displayable { autoPotValue.get() }
    private val autoPotSimulateInventoryValue = BoolValue("AutoPot-SimulateInventory", true).displayable { autoPotValue.get() }
    private val autoPotRegenValue = BoolValue("AutoPot-Regen", true).displayable { autoPotValue.get() }
    private val autoPotUtilityValue = BoolValue("AutoPot-Utility", true).displayable { autoPotValue.get() }
    private val autoPotNotCombatValue = BoolValue("AutoPot-NotCombat", true).displayable { autoPotValue.get() }
    private val autoPotOnGround = BoolValue("AutoPot-OnGround", true).displayable { autoPotValue.get() }
    private var autoPotThrowing = false
    private var autoPotThrowTime = 0
    private var autoPotPot = -1
    private var autoPotThrowAngle = 0f
    private fun autoPotFindPotion(startSlot: Int, endSlot: Int): Int {
        for (i in startSlot until endSlot) {
            if (autoPotFindSinglePotion(i)) {
                return i
            }
        }
        return -1
    }
    private fun autoPotFindSinglePotion(slot: Int): Boolean {
        val stack = mc.thePlayer.inventoryContainer.getSlot(slot).stack

        if (stack == null || stack.item !is ItemPotion || !ItemPotion.isSplash(stack.itemDamage)) {
            return false
        }

        val itemPotion = stack.item as ItemPotion

        if (mc.thePlayer.health <autoPotHealthValue.get() && autoPotRegenValue.get()) {
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
        } else if (autoPotUtilityValue.get()) {
            for (potionEffect in itemPotion.getEffects(stack)) {
                if (potionEffect.potionID != Potion.heal.id && InventoryUtils.isPositivePotionEffect(potionEffect.potionID) && !mc.thePlayer.isPotionActive(potionEffect.potionID)) {
                    return true
                }
            }
        }

        return false
    }

    // Auto Bow
    private val autoBowWaitForBowAimValue = BoolValue("AutoBow-WaitForBowAimBot", true).displayable { autoBowValue.get() }

    // AntiFireball
    private val antiFireBallSwingValue = ListValue("AntiFireBall-Swing", arrayOf("Normal", "Packet", "None"), "Normal").displayable { antiFireBallValue.get() }
    private val antiFireBallrotationValue = BoolValue("AntiFireBall-Rotation", true).displayable { antiFireBallValue.get() }
    private val antiFireBallTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(autoSoupValue.get()) {
            if (!autoSoupTimer.hasTimePassed(autoSoupDelayValue.get().toLong())) {
                return
            }

            val soupInHotbar = InventoryUtils.findItem(36, 45, Items.mushroom_stew)
            if (mc.thePlayer.health <= autoSoupHealthValue.get() && soupInHotbar != -1) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(soupInHotbar - 36))
                mc.netHandler.addToSendQueue(
                    C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer
                        .getSlot(soupInHotbar).stack)
                )
                if (autoSoupBowlValue.equals("Drop")) {
                    mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.DROP_ITEM,
                            BlockPos.ORIGIN, EnumFacing.DOWN)
                    )
                }
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                autoSoupTimer.reset()
                return
            }

            val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.bowl)
            if (autoSoupBowlValue.equals("Move") && bowlInHotbar != -1) {
                if (autoSoupOpenInventoryValue.get() && mc.currentScreen !is GuiInventory) {
                    return
                }

                var bowlMovable = false

                for (i in 9..36) {
                    val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                    if (itemStack == null) {
                        bowlMovable = true
                        break
                    } else if (itemStack.item == Items.bowl && itemStack.stackSize < 64) {
                        bowlMovable = true
                        break
                    }
                }

                if (bowlMovable) {
                    val openInventory = mc.currentScreen !is GuiInventory && autoSoupSimulateInventoryValue.get()

                    if (openInventory) {
                        mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                    }
                    mc.playerController.windowClick(0, bowlInHotbar, 0, 1, mc.thePlayer)
                }
            }

            val soupInInventory = InventoryUtils.findItem(9, 36, Items.mushroom_stew)
            if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
                if (autoSoupOpenInventoryValue.get() && mc.currentScreen !is GuiInventory) {
                    return
                }

                val openInventory = mc.currentScreen !is GuiInventory && autoSoupSimulateInventoryValue.get()
                if (openInventory) {
                    mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                }

                mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer)

                if (openInventory) {
                    mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
                }

                autoSoupTimer.reset()
            }
        }

        if(autoPotValue.get()) {
            when (autoPotThrowMode.get().lowercase()) {
                "up" -> autoPotThrowAngle = -90f
                "forward" -> autoPotThrowAngle = 0f
                "down" -> autoPotThrowAngle = 90f
                "custom" -> autoPotThrowAngle = autoPotThrowAngleOption.get().toFloat()
            }

            if (autoPotNotCombatValue.get() && FDPClient.combatManager.inCombat) return
            if (autoPotOnGround.get() && !mc.thePlayer.onGround) return
            
            if (!autoPotOnGround.get()) {
                val fallingPlayer = FallingPlayer(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                mc.thePlayer.motionX,
                                mc.thePlayer.motionY,
                                mc.thePlayer.motionZ,
                                mc.thePlayer.rotationYaw,
                                mc.thePlayer.moveStrafing,
                                mc.thePlayer.moveForward,
                                mc.thePlayer.jumpMovementFactor)

                val collisionBlock = fallingPlayer.findCollision(20)
                if (mc.thePlayer.posY - (collisionBlock?.y ?: 0) >= (autoPotGroundDistanceValue.get() + 1.01F))
                    return
            }

            if (autoPotThrowing) {
                autoPotThrowTime++
                RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, autoPotThrowAngle))
                if (autoPotThrowTime == autoPotThrowTickValue.get()) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(autoPotPot - 36))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    autoPotPot = -1
                }
                if (autoPotThrowTime >= (autoPotThrowTickValue.get() * 2)) {
                    autoPotThrowTime = 0
                    autoPotThrowing = false
                }
                return
            }
            if (!InventoryUtils.INV_TIMER.hasTimePassed(autoPotDelayValue.get().toLong())) return

            val enableSelect = autoPotSelectValue.get() != -1
            val potion = if (enableSelect) {
                if (autoPotFindSinglePotion(36 + autoPotSelectValue.get())) {
                    36 + autoPotSelectValue.get()
                } else {
                    -1
                }
            } else {
                autoPotFindPotion(36, 45)
            }

            if (potion != -1) {
                RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, autoPotThrowAngle))
                autoPotPot = potion
                autoPotThrowing = true
                InventoryUtils.INV_TIMER.reset()
                return
            }

            if (autoPotOpenInventoryValue.get() && !enableSelect) {
                val invPotion = autoPotFindPotion(9, 36)
                if (invPotion != -1) {
                    val openInventory = mc.currentScreen !is GuiInventory && autoPotSimulateInventoryValue.get()
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

        if(autoBowValue.get()) {
            val bowAimbot = FDPClient.moduleManager[BowAimbot::class.java]!!

            if (mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item == Items.bow &&
                mc.thePlayer.itemInUseDuration > 20 && (!autoBowWaitForBowAimValue.get() || !bowAimbot.state || bowAimbot.hasTarget())) {
                mc.thePlayer.stopUsingItem()
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
        }

        if(antiFireBallValue.get()) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < 5.5 && antiFireBallTimer.hasTimePassed(300)) {
                    if (antiFireBallrotationValue.get()) {
                        RotationUtils.setTargetRotation(RotationUtils.getRotationsNonLivingEntity(entity))
                    }

                    mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

                    when(antiFireBallSwingValue.get().lowercase()) {
                        "normal" -> mc.thePlayer.swingItem()

                        "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    antiFireBallTimer.reset()
                    break
                }
            }
        }
    }
}
