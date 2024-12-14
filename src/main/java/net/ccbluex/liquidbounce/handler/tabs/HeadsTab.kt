/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.tabs

import com.google.gson.JsonParser
import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.extensions.SharedScopes
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class HeadsTab : CreativeTabs("Heads") {

    // List of heads
    private val heads = ArrayList<ItemStack>(512)

    /**
     * Constructor of heads tab
     */
    init {
        backgroundImageName = "item_search.png"

        // Launch the coroutine to load heads asynchronously
        SharedScopes.IO.launch { loadHeads() }
    }

    private suspend fun loadHeads() {
        runCatching {
            LOGGER.info("Loading heads...")

            // Asynchronously fetch the heads configuration
            val (response, _) = withContext(Dispatchers.IO) { get("$CLIENT_CLOUD/heads.json") }
            val headsConfiguration = JsonParser().parse(response)

            // Process the heads configuration
            if (!headsConfiguration.isJsonObject) return

            val headsConf = headsConfiguration.asJsonObject

            if (headsConf["enabled"].asBoolean) {
                val url = headsConf["url"].asString

                LOGGER.info("Loading heads from $url...")

                // Asynchronously fetch the heads data
                val (headsResponse, _) = withContext(Dispatchers.IO) { get(url) }
                val headsElement = JsonParser().parse(headsResponse)

                // Process the heads data
                if (!headsElement.isJsonObject) {
                    LOGGER.error("Something is wrong, the heads json is not a JsonObject!")
                    return
                }

                headsElement.asJsonObject.entrySet().mapTo(heads) { (_, value) ->
                    val headElement = value.asJsonObject

                    ItemUtils.createItem("skull 1 3 {display:{Name:\"${headElement["name"].asString}\"},SkullOwner:{Id:\"${headElement["uuid"].asString}\",Properties:{textures:[{Value:\"${headElement["value"].asString}\"}]}}}")!!
                }

                LOGGER.info("Loaded ${heads.size} heads from HeadDB.")
            } else
                LOGGER.info("Heads are disabled.")
        }.onFailure {
            LOGGER.error("Error while reading heads.", it)
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