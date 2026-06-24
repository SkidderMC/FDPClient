/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.inventory

import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack

data class ToolCandidate(
    val slot: Int,
    val effectiveSpeed: Float,
    val remainingDurability: Int
)

object ToolSelection {
    fun bestHotbarTool(
        stacks: List<ItemStack?>,
        block: Block,
        currentSlot: Int,
        minimumDurability: Int
    ): ToolCandidate? {
        require(minimumDurability >= 0) { "Minimum tool durability must be non-negative" }
        return stacks.take(9).mapIndexedNotNull { slot, stack ->
            stack ?: return@mapIndexedNotNull null
            val remaining = if (stack.isItemStackDamageable) stack.durability else Int.MAX_VALUE
            if (remaining <= minimumDurability) return@mapIndexedNotNull null

            val baseSpeed = stack.getStrVsBlock(block)
            val efficiency = stack.getEnchantmentLevel(Enchantment.efficiency)
            val effectiveSpeed = if (baseSpeed > 1f && efficiency > 0) {
                baseSpeed + efficiency * efficiency + 1f
            } else {
                baseSpeed
            }
            ToolCandidate(slot, effectiveSpeed, remaining)
        }.maxWithOrNull(compareBy<ToolCandidate> { it.effectiveSpeed }
            .thenBy { it.slot == currentSlot }
            .thenBy { it.remainingDurability }
            .thenBy { -it.slot })
    }
}
