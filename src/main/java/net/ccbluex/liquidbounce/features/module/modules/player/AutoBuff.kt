/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
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

object AutoBuff : Module("AutoBuff", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val strength by boolean("Strength", true)
    private val speed by boolean("Speed", true)
    private val regeneration by boolean("Regeneration", true)
    private val fireResistance by boolean("FireResistance", true)

    private val delay by int("Delay", 500, 0..2000)

    private val timer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (!timer.hasTimePassed(delay) || mc.playerController.isInCreativeMode)
            return@handler

        val slot = findBuffPotion() ?: return@handler

        val previousSlot = player.inventory.currentItem

        sendPacket(C09PacketHeldItemChange(slot - 36))
        player.inventory.currentItem = slot - 36

        player.sendUseItem(player.heldItem)
        player.swingItem()

        if (previousSlot != slot - 36) {
            sendPacket(C09PacketHeldItemChange(previousSlot))
            player.inventory.currentItem = previousSlot
        }

        timer.reset()
    }

    private fun findBuffPotion(): Int? {
        val player = mc.thePlayer ?: return null

        for (i in 36..44) {
            val stack = player.inventorySlot(i).stack ?: continue

            val item = stack.item

            if (item !is ItemPotion || stack.isSplashPotion())
                continue

            for (effect in item.getEffects(stack)) {
                val potion = Potion.potionTypes.getOrNull(effect.potionID) ?: continue

                if (player.isPotionActive(potion))
                    continue

                val wanted = when (effect.potionID) {
                    Potion.damageBoost.id -> strength
                    Potion.moveSpeed.id -> speed
                    Potion.regeneration.id -> regeneration
                    Potion.fireResistance.id -> fireResistance
                    else -> false
                }

                if (wanted)
                    return i
            }
        }

        return null
    }
}
