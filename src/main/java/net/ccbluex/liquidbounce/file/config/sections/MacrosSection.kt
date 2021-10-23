package net.ccbluex.liquidbounce.file.config.sections

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.macro.Macro
import net.ccbluex.liquidbounce.file.config.ConfigSection

class MacrosSection : ConfigSection("macros") {
    override fun load(json: JsonObject) {
        LiquidBounce.macroManager.macros.clear()

        val jsonArray = json.getAsJsonArray("macros") ?: return

        for (jsonElement in jsonArray) {
            val macroJson = jsonElement.asJsonObject
            LiquidBounce.macroManager.macros.add(Macro(macroJson.get("key").asInt, macroJson.get("command").asString))
        }
    }

    override fun save(): JsonObject {
        val jsonArray = JsonArray()

        for (macro in LiquidBounce.macroManager.macros) {
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