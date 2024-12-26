/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient.clickGui
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.readJson
import java.io.*

class ClickGuiConfig(file: File) : FileConfig(file) {

    override fun loadDefault() = ClickGui.setDefault()

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        // Regenerate panels and elements in case a script got loaded or removed.
        loadDefault()

        val json = file.readJson() as? JsonObject ?: return
        for (panel in clickGui.panels) {
            try {
                val panelObject = json.getAsJsonObject(panel.name)
                panel.open = panelObject["open"].asBoolean
                panel.isVisible = panelObject["visible"].asBoolean
                panel.x = panelObject["posX"].asInt
                panel.y = panelObject["posY"].asInt

                for (element in panel.elements) {
                    if (element !is ModuleElement) continue
                    if (!panelObject.has(element.module.name)) continue
                    try {
                        val elementObject = panelObject.getAsJsonObject(element.module.name)
                        element.showSettings = elementObject["Settings"].asBoolean
                    } catch (e: Exception) {
                        LOGGER.error(
                            "Error while loading clickgui module element with the name '" + element.module.getName() + "' (Panel Name: " + panel.name + ").",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error while loading clickgui panel with the name '" + panel.name + "'.", e)
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonObject = JsonObject()

        for (panel in clickGui.panels) {
            val panelObject = JsonObject()
            panelObject.run {
                addProperty("open", panel.open)
                addProperty("visible", panel.isVisible)
                addProperty("posX", panel.x)
                addProperty("posY", panel.y)
            }
            for (element in panel.elements) {
                if (element !is ModuleElement) continue
                val elementObject = JsonObject()
                elementObject.addProperty("Settings", element.showSettings)
                panelObject.add(element.module.name, elementObject)
            }
            jsonObject.add(panel.name, panelObject)
        }

        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}