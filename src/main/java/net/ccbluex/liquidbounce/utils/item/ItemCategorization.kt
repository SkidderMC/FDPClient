/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.item

import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemHoe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemSpade
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool

enum class WeaponType(val tag: String, private val predicate: (ItemStack) -> Boolean) {
    ANY("Any", { true }),
    SWORD("Sword", { it.item is ItemSword }),
    AXE("Axe", { it.item is ItemAxe }),
    PICKAXE("Pickaxe", { it.item is ItemPickaxe }),
    SHOVEL("Shovel", { it.item is ItemSpade }),
    HOE("Hoe", { it.item is ItemHoe }),
    KNOCKBACK("Knockback", { it.getEnchantmentLevel(Enchantment.knockback) > 0 }),
    FIRE_ASPECT("FireAspect", { it.getEnchantmentLevel(Enchantment.fireAspect) > 0 });

    fun matches(stack: ItemStack) = predicate(stack)
}

object ItemCategorization {
    fun isSword(stack: ItemStack?) = stack?.item is ItemSword
    fun isAxe(stack: ItemStack?) = stack?.item is ItemAxe
    fun isPickaxe(stack: ItemStack?) = stack?.item is ItemPickaxe
    fun isShovel(stack: ItemStack?) = stack?.item is ItemSpade
    fun isHoe(stack: ItemStack?) = stack?.item is ItemHoe
    fun isTool(stack: ItemStack?) = stack?.item is ItemTool || stack?.item is ItemHoe
    fun isWeapon(stack: ItemStack?) = stack?.let { isSword(it) || isAxe(it) } == true
    fun isBow(stack: ItemStack?) = stack?.item is ItemBow
    fun isFood(stack: ItemStack?) = stack?.item is ItemFood
    fun isPotion(stack: ItemStack?) = stack?.item is ItemPotion
    fun isArmor(stack: ItemStack?) = stack?.item is ItemArmor
    fun isBlock(stack: ItemStack?) = stack?.item is ItemBlock
}
