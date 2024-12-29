/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.tabs

import com.google.gson.JsonObject
import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.io.HttpUtils.get
import net.ccbluex.liquidbounce.utils.io.parseJson
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
            val (response, _) = get("$CLIENT_CLOUD/heads.json")
            val headsConf = response.parseJson() as? JsonObject ?: return

            if (headsConf["enabled"].asBoolean) {
                val url = headsConf["url"].asString

                LOGGER.info("Loading heads from $url...")

                val (headsResponse, _) = get(url)
                val headsElement = headsResponse.parseJson() as? JsonObject ?: run {
                    LOGGER.error("Something is wrong, the heads json is not a JsonObject!")
                    return
                }

                heads = headsElement.entrySet().map { (_, value) ->
                    val headElement = value.asJsonObject

                    ItemUtils.createItem("skull 1 3 {display:{Name:\"${headElement["name"].asString}\"},SkullOwner:{Id:\"${headElement["uuid"].asString}\",Properties:{textures:[{Value:\"${headElement["value"].asString}\"}]}}}")!!
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