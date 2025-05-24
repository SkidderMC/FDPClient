/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.file.gson.json
import net.ccbluex.liquidbounce.file.gson.jsonArray
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeJson
import java.io.File
import java.io.IOException

class HudConfig(file: File) : FileConfig(file) {

    override fun loadDefault() = HUD.setDefault()

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val jsonArray = file.readJson() as? JsonArray ?: return

        HUD.clearElements()

        try {
            for (jsonObject in jsonArray) {
                if (jsonObject !is JsonObject)
                    continue

                if (!jsonObject.has("Type"))
                    continue

                val type = jsonObject["Type"].asString

                try {
                    val elementClass = HUD.ELEMENTS.entries.find { it.value.name == type }?.key

                    if (elementClass == null) {
                        ClientUtils.LOGGER.warn("Unrecognized HUD element: '$type'")
                        continue
                    }

                    val element = elementClass.newInstance()

                    element.x = jsonObject["X"].asDouble
                    element.y = jsonObject["Y"].asDouble
                    element.scale = jsonObject["Scale"].asFloat
                    element.side = Side(
                        Side.Horizontal.getByName(jsonObject["HorizontalFacing"].asString) ?: Side.Horizontal.RIGHT,
                        Side.Vertical.getByName(jsonObject["VerticalFacing"].asString) ?: Side.Vertical.UP
                    )

                    for (value in element.values) {
                        if (jsonObject.has(value.name))
                            value.fromJson(jsonObject[value.name])
                    }

                    // Support for old HUD files
                    if (jsonObject.has("font"))
                        element.values.find { it is FontValue }?.fromJson(jsonObject["font"])

                    HUD.addElement(element)
                } catch (e: Exception) {
                    ClientUtils.LOGGER.error("Error while loading custom HUD element '$type' from config.", e)
                }
            }

            // Add forced elements when missing
            for ((elementClass, info) in HUD.ELEMENTS) {
                if (info.force && HUD.elements.none { it.javaClass == elementClass }) {
                    HUD.addElement(elementClass.newInstance())
                }
            }
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("Error while loading custom hud config.", e)
            HUD.setDefault()
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonArray = jsonArray {
            for (element in HUD.elements) {
                +json {
                    "Type" to element.name
                    "X" to element.x
                    "Y" to element.y
                    "Scale" to element.scale
                    "HorizontalFacing" to element.side.horizontal.sideName
                    "VerticalFacing" to element.side.vertical.sideName

                    element.values.forEach {
                        it.name to it.toJson()
                    }
                }
            }
        }

        file.writeJson(jsonArray)
    }
}