/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.ArmorComparator
import net.ccbluex.liquidbounce.utils.inventory.ArmorSet
import net.ccbluex.liquidbounce.utils.inventory.durability
import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

object ArmorFilter : Module("ArmorFilter", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val minDurabilityPercent by int("MinDurabilityPercent", 10, 0..100)
    private val minProtectionLevel by int("MinProtectionLevel", 0, 0..4)

    private val allowLeather by boolean("Leather", true)
    private val allowGold by boolean("Gold", true)
    private val allowChain by boolean("Chain", true)
    private val allowIron by boolean("Iron", true)
    private val allowDiamond by boolean("Diamond", true)

    fun selectBestArmorSet(stacks: List<ItemStack?>, entityStacksMap: Map<ItemStack, EntityItem>? = null): ArmorSet? {
        if (!handleEvents()) {
            return ArmorComparator.getBestArmorSet(stacks, entityStacksMap)
        }

        val filteredStacks = stacks.map { stack ->
            if (stack?.item is ItemArmor && !isArmorAllowed(stack)) {
                null
            } else {
                stack
            }
        }

        val filteredEntityStacks = entityStacksMap?.filterKeys(::isArmorAllowed)

        return ArmorComparator.getBestArmorSet(filteredStacks, filteredEntityStacks)
    }

    fun isArmorAllowed(stack: ItemStack?): Boolean {
        val armor = stack?.item as? ItemArmor ?: return false

        if (!isMaterialAllowed(armor.armorMaterial)) {
            return false
        }

        if (stack.maxDamage > 0) {
            val durabilityPercent = stack.durability * 100 / stack.maxDamage

            if (durabilityPercent < minDurabilityPercent) {
                return false
            }
        }

        return stack.getEnchantmentLevel(Enchantment.protection) >= minProtectionLevel
    }

    private fun isMaterialAllowed(material: ItemArmor.ArmorMaterial) = when (material) {
        ItemArmor.ArmorMaterial.LEATHER -> allowLeather
        ItemArmor.ArmorMaterial.GOLD -> allowGold
        ItemArmor.ArmorMaterial.CHAIN -> allowChain
        ItemArmor.ArmorMaterial.IRON -> allowIron
        ItemArmor.ArmorMaterial.DIAMOND -> allowDiamond
    }
}
