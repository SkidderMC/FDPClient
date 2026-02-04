/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.inventory.isSplashPotion
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemPotion
import net.minecraft.potion.Potion

object AutoPot : Module("AutoPot", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {

    private val health by float("Health", 15F, 1F..20F) { healPotion || regenerationPotion }
    private val delay by int("Delay", 500, 500..1000)

    // Useful potion options
    private val healPotion by boolean("HealPotion", true)
    private val regenerationPotion by boolean("RegenPotion", true)
    private val fireResistancePotion by boolean("FireResPotion", true)
    private val strengthPotion by boolean("StrengthPotion", true)
    private val jumpPotion by boolean("JumpPotion", true)
    private val speedPotion by boolean("SpeedPotion", true)

    private val openInventory by boolean("OpenInv", false)
    private val simulateInventory by boolean("SimulateInventory", true) { !openInventory }

    private val groundDistance by float("GroundDistance", 2F, 0F..5F)
    private val mode by choices("Mode", arrayOf("Normal", "Jump", "Port"), "Normal")

    private val options = RotationSettings(this).withoutKeepRotation().apply {
        resetTicksValue.excludeWithState()

        immediate = true
    }

    private val msTimer = MSTimer()
    private var potion = -1

    val onRotationUpdate = handler<RotationUpdateEvent> {
        if (!msTimer.hasTimePassed(delay) || mc.playerController.isInCreativeMode)
            return@handler

        val player = mc.thePlayer ?: return@handler

        // Hotbar Potion
        val potionInHotbar = findPotion(36, 44)

        if (potionInHotbar != null) {
            if (player.onGround) {
                when (mode.lowercase()) {
                    "jump" -> player.tryJump()
                    "port" -> player.moveEntity(0.0, 0.42, 0.0)
                }
            }

            // Prevent throwing potions into the void
            val fallingPlayer = FallingPlayer(player)

            val collisionBlock = fallingPlayer.findCollision(20)?.pos

            if (player.posY - (collisionBlock?.y ?: return@handler) - 1 > groundDistance)
                return@handler

            potion = potionInHotbar

            if (player.rotationPitch <= 80F) {
                setTargetRotation(Rotation(player.rotationYaw, nextFloat(80F, 90F)).fixedSensitivity(), options)
            }

            nextTick {
                SilentHotbar.selectSlotSilently(
                    this,
                    potion - 36,
                    ticksUntilReset = 1,
                    immediate = true,
                    render = false,
                    resetManually = true
                )

                if (potion >= 0 && RotationUtils.serverRotation.pitch >= 75F) {
                    player.sendUseItem(player.heldItem)

                    msTimer.reset()
                    potion = -1
                }
            }
            return@handler
        }

        // Inventory Potion -> Hotbar Potion
        val potionInInventory = findPotion(9, 36) ?: return@handler

        if (InventoryUtils.hasSpaceInHotbar()) {
            if (openInventory && mc.currentScreen !is GuiInventory)
                return@handler

            nextTick {
                if (simulateInventory)
                    serverOpenInventory = true

                mc.playerController.windowClick(0, potionInInventory, 0, 1, player)

                if (simulateInventory && mc.currentScreen !is GuiInventory)
                    serverOpenInventory = false

                msTimer.reset()
            }
        }

    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int? {
        val player = mc.thePlayer

        for (i in startSlot..endSlot) {
            val stack = player.inventorySlot(i).stack

            if (stack == null || stack.item !is ItemPotion || !stack.isSplashPotion())
                continue

            val itemPotion = stack.item as ItemPotion

            for (potionEffect in itemPotion.getEffects(stack))
                if (player.health <= health && healPotion && potionEffect.potionID == Potion.heal.id)
                    return i

            if (!player.isPotionActive(Potion.regeneration))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (player.health <= health && regenerationPotion && potionEffect.potionID == Potion.regeneration.id)
                        return i

            if (!player.isPotionActive(Potion.fireResistance))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (fireResistancePotion && potionEffect.potionID == Potion.fireResistance.id)
                        return i

            if (!player.isPotionActive(Potion.moveSpeed))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (speedPotion && potionEffect.potionID == Potion.moveSpeed.id)
                        return i

            if (!player.isPotionActive(Potion.jump))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (jumpPotion && potionEffect.potionID == Potion.jump.id)
                        return i

            if (!player.isPotionActive(Potion.damageBoost))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (strengthPotion && potionEffect.potionID == Potion.damageBoost.id)
                        return i
        }

        return null
    }

    override val tag
        get() = health.toString()

}