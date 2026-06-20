/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils.isConsumingItem
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemAppleGold
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange

/**
 * SmartEat module - Automatically eats food at the right time.
 *
 * Eats the best food in your hotbar once hunger (or, optionally, health) drops
 * below the configured threshold, while you are not already blocking, consuming
 * something, or in danger, then restores the slot you had selected.
 */
object SmartEat : Module(
    "SmartEat", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST,
    gameDetecting = true, spacedName = "Smart Eat"
) {

    private val minHunger by int("MinHunger", 17, 1..20)

    private val eatWhenLowHealth by boolean("EatWhenLowHealth", true)
    private val minHealth by float("MinHealth", 8f, 0f..20f) { eatWhenLowHealth }

    private val onlyWhenSafe by boolean("OnlyWhenSafe", true)

    private val pauseOnCombat by boolean("PauseOnCombat", false)
    private val combatRange by float("CombatRange", 8f, 1f..16f) { pauseOnCombat }

    private val eatGoldenApples by boolean("EatGoldenApples", false)
    private val eatNotchApples by boolean("EatNotchApples", false) { eatGoldenApples }

    // When health drops to/below this value, prefer a (notch) golden apple over the
    // most-saturating regular food. 0 keeps the plain saturation-based pick.
    private val preferGappleHealth by float("PreferGoldenAppleHealth", 0f, 0f..20f) { eatGoldenApples }
    private val preferNotchHealth by float("PreferNotchAppleHealth", 0f, 0f..20f) { eatGoldenApples && eatNotchApples }

    private val silent by boolean("Silent", true)
    // How long (in ticks) to keep the silent slot before swapping back. 0 = immediate.
    private val swapBackDelay by int("SwapBackDelay", 0, 0..40, "ticks") { silent }
    private val swapBack by boolean("SwapBack", true) { !silent }

    // Safety cap for the eat animation; a vanilla food item finishes in 32 ticks.
    private val maxEatTicks by int("MaxEatTicks", 40, 32..60, "ticks")

    private var prevSlot = -1
    private var eatingSlot = -1
    private var eatTicks = 0

    override fun onEnable() {
        reset()
    }

    override fun onDisable() {
        finishEating()
    }

    private fun reset() {
        prevSlot = -1
        eatingSlot = -1
        eatTicks = 0
    }

    private val isEating
        get() = eatingSlot != -1

    private fun isEdible(stack: ItemStack?): Boolean {
        val item = stack?.item ?: return false
        if (item !is ItemFood) return false

        if (item is ItemAppleGold) {
            if (!eatGoldenApples) return false
            // Notch (enchanted) golden apples have metadata 1.
            if (stack.metadata > 0 && !eatNotchApples) return false
        }

        return true
    }

    private fun isGoldenApple(stack: ItemStack, notch: Boolean): Boolean {
        val item = stack.item
        if (item !is ItemAppleGold) return false
        return if (notch) stack.metadata > 0 else stack.metadata == 0
    }

    private fun findGoldenAppleSlot(notch: Boolean): Int {
        val player = mc.thePlayer ?: return -1

        for (slot in 0..8) {
            val stack = player.inventory.getStackInSlot(slot) ?: continue
            if (!isEdible(stack)) continue
            if (isGoldenApple(stack, notch)) return slot
        }

        return -1
    }

    /**
     * Picks the hotbar slot (0..8) holding the food that restores the most
     * saturation, ignoring golden/notch apples unless configured.
     *
     * When health is low enough, a (notch) golden apple is preferred over the
     * saturation pick to favour the regeneration/absorption it grants.
     */
    private fun findBestFood(): Int {
        val player = mc.thePlayer ?: return -1

        // Health-based preference: more urgent (lower) threshold wins first.
        if (eatGoldenApples && eatNotchApples && preferNotchHealth > 0f && player.health <= preferNotchHealth) {
            val notchSlot = findGoldenAppleSlot(true)
            if (notchSlot != -1) return notchSlot
        }
        if (eatGoldenApples && preferGappleHealth > 0f && player.health <= preferGappleHealth) {
            val gappleSlot = findGoldenAppleSlot(false)
            if (gappleSlot != -1) return gappleSlot
        }

        var bestSlot = -1
        var bestSaturation = -1f

        for (slot in 0..8) {
            val stack = player.inventory.getStackInSlot(slot) ?: continue
            if (!isEdible(stack)) continue

            val food = stack.item as ItemFood
            val saturation = food.getHealAmount(stack) + food.getSaturationModifier(stack)

            if (saturation > bestSaturation) {
                bestSaturation = saturation
                bestSlot = slot
            }
        }

        return bestSlot
    }

    /**
     * True when an attackable enemy is within [combatRange].
     */
    private fun isInCombat(): Boolean {
        val world = mc.theWorld ?: return false
        val player = mc.thePlayer ?: return false

        return world.loadedEntityList.toList().any { entity ->
            entity is EntityLivingBase && isSelected(entity, true) &&
                player.getDistanceToEntityBox(entity) <= combatRange
        }
    }

    private fun shouldEat(): Boolean {
        val player = mc.thePlayer ?: return false

        // Don't interfere with another consume/use already in progress.
        if (isConsumingItem() && !isEating)
            return false

        if (mc.gameSettings.keyBindUseItem.isKeyDown)
            return false

        val hungerLow = player.foodStats.foodLevel <= minHunger
        val healthLow = eatWhenLowHealth && player.health <= minHealth

        return hungerLow || healthLow
    }

    private fun startEating(slot: Int) {
        val player = mc.thePlayer ?: return

        eatingSlot = slot
        eatTicks = 0

        if (silent) {
            prevSlot = -1
            val ticksUntilReset = if (swapBackDelay > 0) swapBackDelay else null
            SilentHotbar.selectSlotSilently(this, slot, ticksUntilReset, immediate = true)
        } else {
            if (prevSlot == -1)
                prevSlot = player.inventory.currentItem
            player.inventory.currentItem = slot
        }
    }

    private fun finishEating() {
        val player = mc.thePlayer

        if (silent) {
            SilentHotbar.resetSlot(this, immediate = true)
        } else if (swapBack && prevSlot != -1 && player != null) {
            player.inventory.currentItem = prevSlot
        }

        reset()
    }

    val onWorld = handler<WorldEvent> {
        reset()
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        // A server-forced or unexpected slot change cancels the current eat.
        if (isEating && !silent && (packet is S09PacketHeldItemChange || packet is C09PacketHeldItemChange))
            finishEating()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (onlyWhenSafe && !player.onGround)
            return@handler

        if (isEating) {
            val slot = if (silent) SilentHotbar.currentSlot else player.inventory.currentItem
            val held = player.inventory.getStackInSlot(slot)

            // Item gone (eaten / dropped) or no longer edible -> we are done.
            if (eatTicks > 0 && (held == null || held.item !is ItemFood)) {
                finishEating()
                return@handler
            }

            // Hold right click on the food and keep the eat animation going.
            player.sendUseItem(player.inventory.mainInventory[slot])
            mc.netHandler.addToSendQueue(C03PacketPlayer(player.onGround))

            eatTicks++

            // Safety cap: a vanilla food item finishes in 32 ticks.
            if (eatTicks > maxEatTicks)
                finishEating()

            return@handler
        }

        // Pause when an enemy is nearby, so we don't open ourselves up mid-fight.
        if (pauseOnCombat && isInCombat())
            return@handler

        if (!shouldEat())
            return@handler

        val foodSlot = findBestFood()
        if (foodSlot == -1)
            return@handler

        startEating(foodSlot)
    }

    override val tag: String
        get() = "$minHunger"
}
