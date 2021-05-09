package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.special.macro.Macro
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import java.io.*
import java.nio.charset.StandardCharsets

class MacrosConfig(file: File) : FileConfig(file) {
    override fun loadConfig() {
        val jsonArray = JsonParser().parse(BufferedReader(FileReader(file))).asJsonArray

        for(jsonElement in jsonArray){
            val macroJson=jsonElement.asJsonObject
            LiquidBounce.macroManager.macros.add(Macro(macroJson.get("key").asInt,macroJson.get("command").asString))
        }
    }

    override fun saveConfig() {
        val jsonArray = JsonArray()

        for(macro in LiquidBounce.macroManager.macros){
            val macroJson=JsonObject()
            macroJson.addProperty("key",macro.key)
            macroJson.addProperty("command",macro.command)
            jsonArray.add(macroJson)
        }

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8))
        writer.write(FileManager.PRETTY_GSON.toJson(jsonArray))
        writer.close()
    }
}