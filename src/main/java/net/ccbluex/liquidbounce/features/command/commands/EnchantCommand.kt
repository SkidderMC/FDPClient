/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.NBTTagList
import net.ccbluex.liquidbounce.utils.extensions.set
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction

/**
 * Adds enchantments to the item currently held in the main hand by directly editing the
 * item's "ench" NBT list and resending it through a creative inventory action packet.
 *
 * Subcommands:
 *  - enchant <name|id> [level]   add/overwrite a single enchantment (level defaults to max)
 *  - enchant all [level]         apply every enchantment (level defaults to each enchantment's max)
 *  - enchant clear               remove all enchantments from the held item
 *  - enchant list                list every available enchantment with its id and max level
 *
 * Requires creative mode, identical to [GiveCommand] / [RenameCommand], because it relies on
 * [C10PacketCreativeInventoryAction] to push the edited stack to the server.
 */
object EnchantCommand : Command("enchant", "ench") {

    override fun execute(args: Array<String>) {
        val thePlayer = mc.thePlayer ?: return
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax(
                arrayOf(
                    "<name/id> [level]",
                    "all [level]",
                    "clear",
                    "list"
                )
            )
            return
        }

        when (args[1].lowercase()) {
            "list" -> {
                chat("§c§lEnchantments")
                for (enchantment in allEnchantments()) {
                    chat("§7> §8${displayName(enchantment)} §7(id §8${enchantment.effectId}§7, max §8${enchantment.maxLevel}§7)")
                }
                return
            }
        }

        if (mc.playerController.isNotCreative) {
            chat("§c§lError: §3You need to be in creative mode.")
            return
        }

        val item = thePlayer.heldItem

        if (item?.item == null) {
            chat("§c§lError: §3You need to hold an item.")
            return
        }

        // Decode current enchantments (id -> level), preserving any already present.
        val current = readEnchantments(item.tagCompound)

        when (args[1].lowercase()) {
            "clear" -> {
                if (current.isEmpty()) {
                    chat("§3This item has no enchantments.")
                    return
                }

                current.clear()
                chat("§3Cleared all enchantments.")
            }

            "all" -> {
                val explicitLevel = args.getOrNull(2)?.toIntOrNull()

                if (args.size > 2 && explicitLevel == null) {
                    chat("§c§lError: §3'${args[2]}' is not a valid level.")
                    return
                }

                for (enchantment in allEnchantments()) {
                    current[enchantment.effectId] = explicitLevel ?: enchantment.maxLevel
                }

                chat("§3Applied §8${current.size}§3 enchantments" + (explicitLevel?.let { " at level §8$it§3" } ?: " at max level") + ".")
            }

            else -> {
                val enchantment = resolveEnchantment(args[1])

                if (enchantment == null) {
                    chat("§c§lError: §3Enchantment '${args[1]}' not found. Use §7${usedAlias} list§3.")
                    return
                }

                val explicitLevel = args.getOrNull(2)?.toIntOrNull()

                if (args.size > 2 && explicitLevel == null) {
                    chat("§c§lError: §3'${args[2]}' is not a valid level.")
                    return
                }

                val level = explicitLevel ?: enchantment.maxLevel
                current[enchantment.effectId] = level

                chat("§3Enchanted item with §8${displayName(enchantment)} §8$level§3.")
            }
        }

        writeEnchantments(item.tagCompound ?: NBTTagCompound().also { item.tagCompound = it }, current)
        sendPacket(C10PacketCreativeInventoryAction(36 + thePlayer.inventory.currentItem, item))
        playEdit()
    }

    /**
     * Resolves an enchantment from either its numeric id or its (translated/internal) name.
     */
    private fun resolveEnchantment(input: String): Enchantment? {
        input.toIntOrNull()?.let { return Enchantment.getEnchantmentById(it) }

        return allEnchantments().firstOrNull { enchantment ->
            displayName(enchantment).replace(" ", "").equals(input.replace(" ", ""), ignoreCase = true) ||
                enchantment.name.substringAfterLast('.').equals(input, ignoreCase = true) ||
                enchantment.name.equals(input, ignoreCase = true)
        }
    }

    /**
     * Reads the "ench" NBT list into a mutable id -> level map.
     */
    private fun readEnchantments(tag: NBTTagCompound?): MutableMap<Int, Int> {
        val result = linkedMapOf<Int, Int>()

        tag ?: return result
        if (!tag.hasKey("ench", 9))
            return result

        val list = tag.getTagList("ench", 10)
        repeat(list.tagCount()) {
            val entry = list.getCompoundTagAt(it)
            result[entry.getShort("id").toInt()] = entry.getShort("lvl").toInt()
        }

        return result
    }

    /**
     * Writes the id -> level map back into the "ench" NBT list on [tag].
     */
    private fun writeEnchantments(tag: NBTTagCompound, enchantments: Map<Int, Int>) {
        if (enchantments.isEmpty()) {
            tag.removeTag("ench")
            return
        }

        val list = NBTTagList {
            for ((id, level) in enchantments) {
                appendTag(NBTTagCompound().apply {
                    this["id"] = id.toShort()
                    this["lvl"] = level.toShort()
                })
            }
        }

        tag["ench"] = list
    }

    private fun allEnchantments(): List<Enchantment> =
        (0..255).mapNotNull { Enchantment.getEnchantmentById(it) }

    private fun displayName(enchantment: Enchantment): String =
        runCatching { I18n.format(enchantment.name) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() && !it.startsWith("enchantment.") }
            ?: enchantment.name.substringAfterLast('.')

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> {
                val names = allEnchantments().map { it.name.substringAfterLast('.') }
                (listOf("all", "clear", "list") + names)
                    .filter { it.startsWith(args[0], true) }
            }

            else -> emptyList()
        }
    }
}
