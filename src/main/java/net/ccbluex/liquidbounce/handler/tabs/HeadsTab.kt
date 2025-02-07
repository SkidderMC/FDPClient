/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.tabs

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.io.jsonBody
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class HeadsTab : CreativeTabs("Heads") {

    // List of heads
    private var heads = emptyList<ItemStack>()

    /**
     * Constructor of heads tab
     */
    init {
        backgroundImageName = "item_search.png"

        // Launch the coroutine to load heads asynchronously
        SharedScopes.IO.launch { loadHeads() }
    }

    private fun loadHeads() {
        try {
            LOGGER.info("Loading heads...")

            // Asynchronously fetch the heads configuration
            val headsConf = HttpClient.get("$CLIENT_CLOUD/heads.json").jsonBody<HeadsConfiguration>() ?: return

            if (headsConf.enabled) {
                val url = headsConf.url

                LOGGER.info("Loading heads from $url...")

                val headsMap = HttpClient.get(url).jsonBody<Map<String, HeadInfo>>() ?: return

                heads = headsMap.values.map { head ->
                    ItemUtils.createItem("skull 1 3 {display:{Name:\"${head.name}\"},SkullOwner:{Id:\"${head.uuid}\",Properties:{textures:[{Value:\"${head.value}\"}]}}}")!!
                }

                LOGGER.info("Loaded ${heads.size} heads from HeadDB.")
            } else {
                LOGGER.info("Heads are disabled.")
            }
        } catch (e: Exception) {
            LOGGER.error("Error while reading heads.", e)
        }
    }

    /**
     * Add all items to tab
     *
     * @param itemList list of tab items
     */
    override fun displayAllReleventItems(itemList: MutableList<ItemStack>) {
        itemList += heads
    }

    /**
     * Return icon item of tab
     *
     * @return icon item
     */
    override fun getTabIconItem(): Item = Items.skull

    /**
     * Return name of tab
     *
     * @return tab name
     */
    override fun getTranslatedTabLabel() = "Heads"

    /**
     * @return searchbar status
     */
    override fun hasSearchBar() = true
}

private class HeadsConfiguration(val enabled: Boolean, val url: String)

// Only includes needed fields
private class HeadInfo(val name: String, val uuid: String, val value: String)