/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.tabs

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class BlocksTab : CreativeTabs("Special blocks") {

    private val itemStacks by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(
            ItemStack(Blocks.command_block),
            ItemStack(Items.command_block_minecart),
            ItemStack(Blocks.barrier),
            ItemStack(Blocks.dragon_egg),
            ItemStack(Blocks.brown_mushroom_block),
            ItemStack(Blocks.red_mushroom_block),
            ItemStack(Blocks.farmland),
            ItemStack(Blocks.mob_spawner),
            ItemStack(Blocks.lit_furnace)
        )
    }

    /**
     * Initialize of special blocks tab
     */
    init {
        backgroundImageName = "item_search.png"
    }

    /**
     * Add all items to tab
     *
     * @param itemList list of tab items
     */
    override fun displayAllReleventItems(itemList: MutableList<ItemStack>) {
        itemList += itemStacks
    }

    /**
     * Return icon item of tab
     *
     * @return icon item
     */
    override fun getTabIconItem(): Item = ItemStack(Blocks.command_block).item

    /**
     * Return name of tab
     *
     * @return tab name
     */
    override fun getTranslatedTabLabel() = "Special blocks"

    /**
     * @return searchbar status
     */
    override fun hasSearchBar() = true
}