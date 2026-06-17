/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.inventory.enchantments
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.minecraft.client.resources.I18n
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

/**
 * Inspects the item currently held in the main hand and prints all of its data:
 * registry name, numeric id, metadata, stack size, durability, display name, lore,
 * enchantments and the raw NBT tag. The raw NBT is also copied to the clipboard so it
 * can be pasted straight into [GiveCommand] as a datatag.
 *
 * Non-destructive: it only reads from the held [ItemStack].
 */
object ItemDataCommand : Command("itemdata", "nbt", "iteminfo") {

    override fun execute(args: Array<String>) {
        val thePlayer = mc.thePlayer ?: return

        val stack: ItemStack? = thePlayer.heldItem

        if (stack == null || stack.item == null) {
            chat("§c§lError: §3You need to hold an item.")
            return
        }

        val item = stack.item
        val registryName = Item.itemRegistry.getNameForObject(item)?.toString() ?: "unknown"

        chat("§c§lItem Data")
        chat("§7> Name: §8${stack.displayName}")
        chat("§7> Registry: §8$registryName")
        chat("§7> ID: §8${Item.getIdFromItem(item)} §7Meta: §8${stack.metadata}")
        chat("§7> Count: §8${stack.stackSize}§7/§8${stack.maxStackSize}")

        if (stack.isItemStackDamageable) {
            val durability = ItemUtils.getItemDurability(stack)
            chat("§7> Durability: §8$durability§7/§8${stack.maxDamage}")
        }

        // Lore (display.Lore)
        val lore = readLore(stack)
        if (lore.isNotEmpty()) {
            chat("§7> Lore:")
            for (line in lore)
                chat("§8  - §7$line")
        }

        // Enchantments
        val enchantments = stack.enchantments
        if (enchantments.isNotEmpty()) {
            chat("§7> Enchantments (§8${enchantments.size}§7):")
            for ((enchantment, level) in enchantments) {
                @Suppress("SENSELESS_COMPARISON")
                if (enchantment == null)
                    continue

                val displayName = runCatching { I18n.format(enchantment.name) }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() && !it.startsWith("enchantment.") }
                    ?: enchantment.name

                chat("§8  - §7$displayName §8${level} §7(id §8${enchantment.effectId}§7)")
            }
        }

        // Raw NBT
        val tag = stack.tagCompound
        if (tag != null) {
            val nbtString = tag.toString()
            chat("§7> NBT: §8$nbtString")
            runCatching { MiscUtils.copy(nbtString) }
            chat("§7> §aNBT copied to clipboard.")
        } else {
            chat("§7> NBT: §8(none)")
        }
    }

    /**
     * Reads the lore lines stored under the vanilla "display.Lore" NBT path.
     */
    private fun readLore(stack: ItemStack): List<String> {
        val tag = stack.tagCompound ?: return emptyList()

        if (!tag.hasKey("display", 10))
            return emptyList()

        val display = tag.getCompoundTag("display")

        // 9 = TAG_List, the inner list holds strings (type 8)
        if (!display.hasKey("Lore", 9))
            return emptyList()

        val loreList = display.getTagList("Lore", 8)
        return (0 until loreList.tagCount()).map { loreList.getStringTagAt(it) }
    }
}
