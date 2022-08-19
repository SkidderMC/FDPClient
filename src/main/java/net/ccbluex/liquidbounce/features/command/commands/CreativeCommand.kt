/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnumEnchantmentType


class CreativeCommand : Command("Creative", arrayOf("Items", "gmc")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            when {
                args[1].equals("rename", ignoreCase = true) -> {
                    if (args.size > 1) {
                        if (mc.playerController.isNotCreative) {
                        chat("§c§lError: §3You need to be in creative mode.")
                        return
                        }

                        val item = mc.thePlayer.heldItem
                        if (item == null || item.item == null) {
                        chat("§c§lError: §3You need to hold a item.")
                        return
                        }

                        item.setStackDisplayName(ColorUtils.translateAlternateColorCodes(StringUtils.toCompleteString(args, 1)))
                        chat("§3Item renamed to '${item.displayName}§3'")
                        return
                    }
                    chatSyntax("creative rename <name>")
                    }
                }

                args[1].equals("holostand", ignoreCase = true) -> {
                    if (args.size > 4) {
                        if (mc.playerController.isNotCreative) {
                        chat("§c§lError: §3You need to be in creative mode.")
                        return
                        }

                            try {
                                val x = args[1].toDouble()
                                val y = args[2].toDouble()
                                val z = args[3].toDouble()
                                val message = StringUtils.toCompleteString(args, 4)

                                val itemStack = ItemStack(Items.armor_stand)
                                val base = NBTTagCompound()
                                val entityTag = NBTTagCompound()
                                entityTag.setInteger("Invisible", 1)
                                entityTag.setString("CustomName", message)
                                entityTag.setInteger("CustomNameVisible", 1)
                                entityTag.setInteger("NoGravity", 1)
                                val position = NBTTagList()
                                position.appendTag(NBTTagDouble(x))
                                position.appendTag(NBTTagDouble(y))
                                position.appendTag(NBTTagDouble(z))
                                entityTag.setTag("Pos", position)
                                base.setTag("EntityTag", entityTag)
                                itemStack.tagCompound = base
                                itemStack.setStackDisplayName("§c§lHolo§eStand")
                                mc.netHandler.addToSendQueue(C10PacketCreativeInventoryAction(36, itemStack))

                                chat("The HoloStand was successfully added to your inventory.")
                            } catch (exception: NumberFormatException) {
                                chatSyntaxError()
                            }
                            return
                        }
                        chatSyntax("creative holostand <x> <y> <z> <message...>")
                }

                args[1].equals("enchant", ignoreCase = true) -> {
                    if (args.size > 2) {
                        if (mc.playerController.isNotCreative) {
                        chat("§c§lError: §3You need to be in creative mode.")
                        return
                    }

                    val item = mc.thePlayer.heldItem

                    if (item == null || item.item == null) {
                        chat("§c§lError: §3You need to hold an item.")
                        return
                    }

                    val enchantID = try {
                        args[1].toInt()
                    } catch (e: NumberFormatException) {
                        val enchantment = Enchantment.getEnchantmentByLocation(args[1])

                        if (enchantment == null) {
                        chat("There is no enchantment with the name '${args[1]}'")
                        return
                        }

                        enchantment.effectId
                    }

                    val enchantment = Enchantment.getEnchantmentById(enchantID)
                    if (enchantment == null) {
                    chat("There is no enchantment with the ID '$enchantID'")
                    return
                    }

                    val level = try {
                    args[2].toInt()
                    } catch (e: NumberFormatException) {
                        chatSyntaxError()
                        return
                    }

                    item.addEnchantment(enchantment, level)
                    chat("${enchantment.getTranslatedName(level)} added to ${item.displayName}.")
                    return
                }
                    chatSyntax("creative enchant <type> [level]")
                }

                args[1].equals("give", ignoreCase = true) -> {
                    if (mc.playerController.isNotCreative) {
                        chat("§c§lError: §3You need to be in creative mode.")
                        return
                    }

                if (args.size > 1) {
                val itemStack = ItemUtils.createItem(StringUtils.toCompleteString(args, 1))

                if (itemStack == null) {
                    chatSyntaxError()
                    return
                }

                var emptySlot = -1

                for (i in 36..44) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i).stack == null) {
                        emptySlot = i
                    break
                }
            }

                if (emptySlot == -1) {
                    for (i in 9..44) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).stack == null) {
                            emptySlot = i
                            break
                        }
                    }
                }

                if (emptySlot != -1) {
                    mc.netHandler.addToSendQueue(C10PacketCreativeInventoryAction(emptySlot, itemStack))
                    chat("§7Given [§8${itemStack.displayName}§7] * §8${itemStack.stackSize}§7 to §8${mc.getSession().username}§7.")
                } else
                    chat("Your inventory is full.")
            return
            }

            chatSyntax("creative give <item> [amount] [data] [datatag]")
            }
        }
        chatSyntax("creative <rename/holostand/enchant/give>")
    }
}
