/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.item

import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack

/** Produces a stable weighted score from protocol-47 enchantment NBT. */
class EnchantmentValueEstimator(
    private vararg val enchantments: WeightedEnchantment,
) : Comparator<ItemStack> {

    fun estimateValue(stack: ItemStack): Float = enchantments.sumOf {
        stack.getEnchantmentLevel(it.enchantment).toDouble() * it.factor
    }.toFloat()

    override fun compare(first: ItemStack, second: ItemStack): Int =
        estimateValue(first).compareTo(estimateValue(second))

    data class WeightedEnchantment(val enchantment: Enchantment, val factor: Float)
}
