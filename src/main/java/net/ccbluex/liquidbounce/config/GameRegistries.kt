/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.potion.Potion

/** Stable 1.8 registry maps for registry-backed multi-select values. */
object GameRegistries {

    val blocks: Map<String, Block>
        get() = Block.blockRegistry.keys.mapNotNull { key ->
            Block.blockRegistry.getObject(key)?.let { key.toString() to it }
        }.toMap()

    val items: Map<String, Item>
        get() = Item.itemRegistry.keys.mapNotNull { key ->
            Item.itemRegistry.getObject(key)?.let { key.toString() to it }
        }.toMap()

    val effects: Map<String, Potion>
        get() = Potion.potionTypes.mapIndexedNotNull { id, potion ->
            potion?.let { "$id:${it.name.removePrefix("potion.")}" to it }
        }.toMap()
}
