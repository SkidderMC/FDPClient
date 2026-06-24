/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.inventory.isSplashPotion
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

object AutoBuff : Module("AutoBuff", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val strength by boolean("Strength", true)
        .describe("Drink strength potions when not active.")
    private val speed by boolean("Speed", true)
        .describe("Drink speed potions when not active.")
    private val regeneration by boolean("Regeneration", true)
        .describe("Drink regeneration potions when not active.")
    private val fireResistance by boolean("FireResistance", true)
        .describe("Drink fire resistance potions when not active.")

    private val regenerationHealth by float("RegenerationHealth", 14f, 1f..20f, " health") { regeneration }
        .describe("Only refresh regeneration at or below this health.")
    private val refreshBeforeExpiry by int("RefreshBeforeExpiry", 1, 0..10, " seconds")
        .describe("Refresh an enabled effect when less than this duration remains.")

    private val delay by int("Delay", 500, 0..2000)
        .describe("Delay between potion drinks in milliseconds.")

    private val timer = MSTimer()
    private var activeSlot: Int? = null
    private var previousSlot = -1
    private var activeTicks = 0

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (activeSlot != null) {
            updateActiveUse()
            return@handler
        }

        if (!timer.hasTimePassed(delay) || mc.playerController.isInCreativeMode || player.isUsingItem)
            return@handler

        val candidate = findBuffPotion() ?: return@handler
        beginUse(candidate.slot)
    }

    private fun beginUse(slot: Int) {
        val player = mc.thePlayer ?: return
        val stack = player.inventorySlot(slot).stack ?: return

        previousSlot = player.inventory.currentItem
        activeSlot = slot
        activeTicks = 0

        sendPacket(C09PacketHeldItemChange(slot - 36))
        player.inventory.currentItem = slot - 36

        player.sendUseItem(stack)
        if (!player.isUsingItem) finishUse()
    }

    private fun updateActiveUse() {
        val player = mc.thePlayer ?: return clearUseState()
        activeTicks++

        if (player.isUsingItem && activeTicks <= MAX_USE_TICKS) return
        finishUse()
    }

    private fun finishUse() {
        val player = mc.thePlayer
        if (player != null && previousSlot in 0..8) {
            sendPacket(C09PacketHeldItemChange(previousSlot))
            player.inventory.currentItem = previousSlot
        }
        clearUseState()
        timer.reset()
    }

    private fun clearUseState() {
        activeSlot = null
        previousSlot = -1
        activeTicks = 0
    }

    override fun onDisable() {
        if (activeSlot != null) finishUse() else clearUseState()
    }

    val onWorld = handler<WorldEvent> {
        clearUseState()
        timer.reset()
    }

    private fun findBuffPotion(): BuffCandidate? {
        val player = mc.thePlayer ?: return null
        var best: BuffCandidate? = null

        for (i in 36..44) {
            val stack = player.inventorySlot(i).stack ?: continue

            val item = stack.item

            if (item !is ItemPotion || stack.isSplashPotion())
                continue

            for (effect in item.getEffects(stack).orEmpty()) {
                val potion = Potion.potionTypes.getOrNull(effect.potionID) ?: continue
                val priority = priority(effect) ?: continue
                val activeEffect = player.getActivePotionEffect(potion)
                if (activeEffect != null && activeEffect.duration > refreshBeforeExpiry * 20) continue
                if (effect.potionID == Potion.regeneration.id && player.health > regenerationHealth) continue

                val candidate = BuffCandidate(i, priority)
                if (best == null || candidate.priority > best.priority) best = candidate
            }
        }

        return best
    }

    private fun priority(effect: PotionEffect): Int? = when (effect.potionID) {
        Potion.regeneration.id -> if (regeneration) 400 else null
        Potion.fireResistance.id -> if (fireResistance) 300 else null
        Potion.damageBoost.id -> if (strength) 200 else null
        Potion.moveSpeed.id -> if (speed) 100 else null
        else -> null
    }

    private data class BuffCandidate(val slot: Int, val priority: Int)

    private const val MAX_USE_TICKS = 40
}
