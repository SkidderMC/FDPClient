/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

private fun scriptItems(tabObject: JSObject): Array<ItemStack> {
    val converted = ScriptUtils.convert(tabObject.getMember("items"), Array<ItemStack>::class.java)
    val items = converted as? Array<*> ?: error("Script tab items must be an array.")
    return items.mapIndexed { index, item ->
        item as? ItemStack ?: error("Script tab item at index $index must be an ItemStack.")
    }.toTypedArray()
}

class ScriptTab(private val tabObject: JSObject) : CreativeTabs(tabObject.getMember("name") as String) {
    val items = scriptItems(tabObject)

    override fun getTabIconItem() = ItemUtils.createItem(tabObject.getMember("icon") as String)?.item

    override fun getTranslatedTabLabel() = tabObject.getMember("name") as String

    override fun displayAllReleventItems(items: MutableList<ItemStack>) {
        items.addAll(this.items)
    }
}
