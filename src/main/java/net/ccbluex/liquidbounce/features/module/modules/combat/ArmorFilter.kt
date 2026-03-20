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
import net.ccbluex.liquidbounce.utils.inventory.enchantmentSum
import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.ccbluex.liquidbounce.utils.inventory.totalDurability
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

object ArmorFilter : Module("ArmorFilter", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val priority by choices("Priority", arrayOf("Balanced", "Defense", "Durability", "Enchantments"), "Balanced")
    private val keepBackups by int("KeepBackups", 0, 0..3)

    private val minMaterial by choices("MinMaterial", arrayOf("Any", "Leather", "Gold", "Chain", "Iron", "Diamond"), "Any")
    private val minDurabilityPercent by int("MinDurabilityPercent", 10, 0..100)
    private val minProtectionLevel by int("MinProtectionLevel", 0, 0..4)
    private val minEnchantmentSum by int("MinEnchantmentSum", 0, 0..12)

    fun selectBestArmorSet(stacks: List<ItemStack?>, entityStacksMap: Map<ItemStack, EntityItem>? = null): ArmorSet? {
        if (!handleEvents()) {
            return ArmorComparator.getBestArmorSet(stacks, entityStacksMap)
        }

        val armorMap = getArmorCandidatesByType(stacks, entityStacksMap, respectFilter = true)
        val armorSet = ArmorSet(
            armorMap[0]?.firstOrNull(),
            armorMap[1]?.firstOrNull(),
            armorMap[2]?.firstOrNull(),
            armorMap[3]?.firstOrNull()
        )

        return (0..3).any { armorSet[it] != null }.takeIf { it }?.let { armorSet }
    }

    fun getArmorCandidates(
        stacks: List<ItemStack?>,
        armorType: Int,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
        respectFilter: Boolean = true,
        priorityMode: String = priority,
    ): List<Pair<Int?, ItemStack>> =
        getArmorCandidatesByType(stacks, entityStacksMap, respectFilter, priorityMode)[armorType].orEmpty()

    fun getUsefulArmorPieces(
        stacks: List<ItemStack?>,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
    ): Set<ItemStack> {
        if (!handleEvents()) {
            val bestArmorSet = ArmorComparator.getBestArmorSet(stacks, entityStacksMap) ?: return emptySet()
            return bestArmorSet.mapNotNullTo(linkedSetOf()) { it?.second }
        }

        val keepPerType = (keepBackups + 1).coerceAtLeast(1)

        return getArmorCandidatesByType(stacks, entityStacksMap, respectFilter = true)
            .values
            .flatMapTo(linkedSetOf()) { candidates -> candidates.take(keepPerType).map { it.second } }
    }

    fun isUsefulArmor(
        stack: ItemStack?,
        stacks: List<ItemStack?>,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
    ): Boolean {
        val armorStack = stack ?: return false
        return armorStack in getUsefulArmorPieces(stacks, entityStacksMap)
    }

    fun isArmorAllowed(stack: ItemStack?): Boolean {
        val armor = stack?.item as? ItemArmor ?: return false

        if (armorMaterialRank(armor.armorMaterial) < minimumMaterialRank()) {
            return false
        }

        if (stack.maxDamage > 0) {
            val durabilityPercent = stack.durability * 100 / stack.maxDamage

            if (durabilityPercent < minDurabilityPercent) {
                return false
            }
        }

        if (stack.getEnchantmentLevel(Enchantment.protection) < minProtectionLevel) {
            return false
        }

        return stack.enchantmentSum >= minEnchantmentSum
    }

    fun scoreArmorStack(stack: ItemStack, priorityMode: String = priority): Double {
        val defense = armorDefenseScore(stack)
        val protection = stack.getEnchantmentLevel(Enchantment.protection).toDouble()
        val enchantments = stack.enchantmentSum.toDouble()
        val durabilityScore =
            if (stack.maxDamage > 0) stack.totalDurability.toDouble() / stack.maxDamage else stack.totalDurability.toDouble()

        return when (priorityMode) {
            "Defense" -> defense * 1000 + protection * 100 + enchantments * 15 + durabilityScore
            "Durability" -> durabilityScore * 100 + defense * 1000 + enchantments * 10 + protection * 50
            "Enchantments" -> enchantments * 250 + protection * 150 + defense * 1000 + durabilityScore
            else -> defense * 1000 + enchantments * 60 + protection * 100 + durabilityScore * 20
        }
    }

    private fun getArmorCandidatesByType(
        stacks: List<ItemStack?>,
        entityStacksMap: Map<ItemStack, EntityItem>? = null,
        respectFilter: Boolean = true,
        priorityMode: String = priority,
    ): Map<Int, List<Pair<Int?, ItemStack>>> {
        val shouldFilter = handleEvents() && respectFilter

        val filteredStacks = if (shouldFilter) {
            stacks.map { stack ->
                if (stack?.item is ItemArmor && !isArmorAllowed(stack)) null else stack
            }
        } else {
            stacks
        }

        val filteredEntityStacks = if (shouldFilter) {
            entityStacksMap?.filterKeys(::isArmorAllowed)
        } else {
            entityStacksMap
        }

        return ArmorComparator.getSortedArmorPieces(filteredStacks, filteredEntityStacks)
            .mapValues { (_, stacksForType) -> stacksForType.sortedByDescending { scoreArmorStack(it.second, priorityMode) } }
    }

    private fun armorDefenseScore(stack: ItemStack): Double {
        val armor = stack.item as? ItemArmor ?: return .0

        return armor.armorMaterial.getDamageReductionAmount(armor.armorType).toDouble() +
                stack.getEnchantmentLevel(Enchantment.protection) * 0.75 +
                stack.getEnchantmentLevel(Enchantment.projectileProtection) * 0.20 +
                stack.getEnchantmentLevel(Enchantment.blastProtection) * 0.18 +
                stack.getEnchantmentLevel(Enchantment.fireProtection) * 0.12 +
                if (armor.armorType == 3) stack.getEnchantmentLevel(Enchantment.featherFalling) * 0.15 else .0
    }

    private fun minimumMaterialRank() = when (minMaterial) {
        "Leather" -> 0
        "Gold" -> 1
        "Chain" -> 2
        "Iron" -> 3
        "Diamond" -> 4
        else -> Int.MIN_VALUE
    }

    private fun armorMaterialRank(material: ItemArmor.ArmorMaterial) = when (material) {
        ItemArmor.ArmorMaterial.LEATHER -> 0
        ItemArmor.ArmorMaterial.GOLD -> 1
        ItemArmor.ArmorMaterial.CHAIN -> 2
        ItemArmor.ArmorMaterial.IRON -> 3
        ItemArmor.ArmorMaterial.DIAMOND -> 4
        else -> Int.MAX_VALUE
    }
}
