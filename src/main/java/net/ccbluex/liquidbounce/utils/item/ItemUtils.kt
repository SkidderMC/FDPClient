/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.item

import net.ccbluex.liquidbounce.utils.RegexUtils
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

/**
 * @author MCModding4K
 */
object ItemUtils {
    fun getEnchantment(itemStack: ItemStack, enchantment: Enchantment): Int {
        if (itemStack.enchantmentTagList == null || itemStack.enchantmentTagList.hasNoTags()) return 0
        for (i in 0 until itemStack.enchantmentTagList.tagCount()) {
            val tagCompound = itemStack.enchantmentTagList.getCompoundTagAt(i)
            if (tagCompound.hasKey("ench") && tagCompound.getShort("ench").toInt() == enchantment.effectId ||
                tagCompound.hasKey("id") && tagCompound.getShort("id").toInt() == enchantment.effectId) {
                return tagCompound.getShort("lvl").toInt()
            }
        }
        return 0
    }

    fun getEnchantmentCount(itemStack: ItemStack): Int {
        if (itemStack.enchantmentTagList == null || itemStack.enchantmentTagList.hasNoTags()) return 0
        var c = 0
        for (i in 0 until itemStack.enchantmentTagList.tagCount()) {
            val tagCompound = itemStack.enchantmentTagList.getCompoundTagAt(i)
            if (tagCompound.hasKey("ench") || tagCompound.hasKey("id")) {
                c++
            }
        }
        return c
    }

    @JvmStatic
    fun getItemDurability(stack: ItemStack?): Int {
        return if (stack == null) 0 else stack.maxDamage - stack.itemDamage
    }

    fun getWeaponEnchantFactor(
        stack: ItemStack,
        nbtedPriority: Float = 0f,
        goal: EnumNBTPriorityType = EnumNBTPriorityType.NONE
    ): Double {
        return (1.25 * getEnchantment(stack, Enchantment.sharpness)) +
                (1.0 * getEnchantment(stack, Enchantment.fireAspect)) +
                if (hasNBTGoal(stack, goal)) { nbtedPriority } else { 0f }
    }

    private val armorDamageReduceEnchantments = arrayOf(Enchant(Enchantment.protection, 0.06f), Enchant(Enchantment.projectileProtection, 0.032f), Enchant(Enchantment.fireProtection, 0.0585f), Enchant(Enchantment.blastProtection, 0.0304f))
    private val otherArmorEnchantments = arrayOf(Enchant(Enchantment.featherFalling, 3.0f), Enchant(Enchantment.thorns, 1.0f), Enchant(Enchantment.respiration, 0.1f), Enchant(Enchantment.aquaAffinity, 0.05f), Enchant(Enchantment.unbreaking, 0.01f))

    fun compareArmor(
        o1: ArmorPiece,
        o2: ArmorPiece,
        nbtedPriority: Float = 0f,
        goal: EnumNBTPriorityType = EnumNBTPriorityType.NONE
    ): Int {
        // For damage reduction it is better if it is smaller, so it has to be inverted
        // The decimal values have to be rounded since in double math equals is inaccurate
        // For example 1.03 - 0.41 = 0.6200000000000001 and (1.03 - 0.41) == 0.62 would be false
        val compare = RegexUtils.round(getArmorThresholdedDamageReduction(o2.itemStack).toDouble() - if (hasNBTGoal(o2.itemStack, goal)) { nbtedPriority / 5f } else { 0f }, 3)
            .compareTo(RegexUtils.round(getArmorThresholdedDamageReduction(o1.itemStack).toDouble() - if (hasNBTGoal(o1.itemStack, goal)) { nbtedPriority / 5f } else { 0f }, 3))

        // If both armor pieces have the exact same damage, compare enchantments
        if (compare == 0) {
            val otherEnchantmentCmp = RegexUtils.round(getArmorEnchantmentThreshold(o1.itemStack).toDouble(), 3)
                .compareTo(RegexUtils.round(getArmorEnchantmentThreshold(o2.itemStack).toDouble(), 3))

            // If both have the same enchantment threshold, prefer the item with more enchantments
            if (otherEnchantmentCmp == 0) {
                val enchantmentCountCmp = getEnchantmentCount(o1.itemStack).compareTo(getEnchantmentCount(o2.itemStack))
                if (enchantmentCountCmp != 0) {
                    return enchantmentCountCmp
                }

                // Then durability...
                val o1a = o1.itemStack.item as ItemArmor
                val o2a = o2.itemStack.item as ItemArmor
                val durabilityCmp = o1a.armorMaterial.getDurability(o1a.armorType).compareTo(o2a.armorMaterial.getDurability(o2a.armorType))

                return if (durabilityCmp != 0) {
                    durabilityCmp
                } else {
                    // last compare: enchantability...
                    o1a.armorMaterial.enchantability.compareTo(o2a.armorMaterial.enchantability)
                }
            }
            return otherEnchantmentCmp
        }
        return compare
    }

    private fun getArmorThresholdedDamageReduction(itemStack: ItemStack): Float {
        val item = itemStack.item as ItemArmor
        return getArmorDamageReduction(item.armorMaterial.getDamageReductionAmount(item.armorType), 0) * (1 - getArmorThresholdedEnchantmentDamageReduction(itemStack))
    }

    private fun getArmorDamageReduction(defensePoints: Int, toughness: Int): Float {
        return 1 - 20.0f.coerceAtMost((defensePoints / 5.0f).coerceAtLeast(defensePoints - 1 / (2 + toughness / 4.0f))) / 25.0f
    }

    private fun getArmorThresholdedEnchantmentDamageReduction(itemStack: ItemStack): Float {
        var sum = 0.0f
        for (i in armorDamageReduceEnchantments.indices) {
            sum += getEnchantment(itemStack, armorDamageReduceEnchantments[i].enchantment) * armorDamageReduceEnchantments[i].factor
        }
        return sum
    }

    private fun getArmorEnchantmentThreshold(itemStack: ItemStack): Float {
        var sum = 0.0f
        for (i in otherArmorEnchantments.indices) {
            sum += getEnchantment(itemStack, otherArmorEnchantments[i].enchantment) * otherArmorEnchantments[i].factor
        }
        return sum
    }

    class Enchant(val enchantment: Enchantment, val factor: Float)

    fun hasNBTGoal(stack: ItemStack, goal: EnumNBTPriorityType): Boolean {

        if (stack.hasTagCompound() && stack.tagCompound.hasKey("display", 10)) {
            val display = stack.tagCompound.getCompoundTag("display")

            if (goal == EnumNBTPriorityType.HAS_DISPLAY_TAG) {
                return true
            } else if (goal == EnumNBTPriorityType.HAS_NAME) {
                return display.hasKey("Name")
            } else if (goal == EnumNBTPriorityType.HAS_LORE) {
                return display.hasKey("Lore") && display.getTagList("Lore", 8).tagCount()> 0
            }
        }

        return false
    }

    enum class EnumNBTPriorityType {
        HAS_NAME,
        HAS_LORE,
        HAS_DISPLAY_TAG,
        NONE // for default
    }
}