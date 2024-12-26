/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import me.liuli.elixir.utils.set
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.utils.io.readJson
import java.io.*

class ModulesConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val json = file.readJson() as? JsonObject ?: return

        for ((key, value) in json.entrySet()) {
            val module = moduleManager[key] ?: continue

            val jsonModule = value as JsonObject
            module.state = jsonModule["State"].asBoolean
            module.keyBind = jsonModule["KeyBind"].asInt
            if (jsonModule.has("Array")) module.inArray = jsonModule["Array"].asBoolean
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
        for (module in moduleManager) {
            val jsonMod = JsonObject()
            jsonMod.run {
                addProperty("State", module.state)
                addProperty("KeyBind", module.keyBind)
                addProperty("Array", module.inArray)
            }
            jsonObject[module.name] = jsonMod
        }
        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}