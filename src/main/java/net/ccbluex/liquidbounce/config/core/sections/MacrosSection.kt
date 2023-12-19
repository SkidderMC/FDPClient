/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config.core.sections

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.handler.macro.Macro
import net.ccbluex.liquidbounce.config.core.ConfigSection

class MacrosSection : ConfigSection("macros") {
    override fun load(json: JsonObject) {
        FDPClient.macroManager.macros.clear()

        val jsonArray = json.getAsJsonArray("macros") ?: return

        for (jsonElement in jsonArray) {
            val macroJson = jsonElement.asJsonObject
            FDPClient.macroManager.macros.add(Macro(macroJson.get("key").asInt, macroJson.get("command").asString))
        }
    }

    override fun save(): JsonObject {
        val jsonArray = JsonArray()

        for (macro in FDPClient.macroManager.macros) {
            val macroJson = JsonObject()
            macroJson.addProperty("key", macro.key)
            macroJson.addProperty("command", macro.command)
            jsonArray.add(macroJson)
        }

        val json = JsonObject()
        json.add("macros", jsonArray)
        return json
    }
}