/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.file.gson.asBooleanOrNull
import net.ccbluex.liquidbounce.file.gson.asIntOrNull
import net.ccbluex.liquidbounce.file.gson.json
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
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
            runCatching {
                val module = moduleManager[key] ?: return@runCatching
                val jsonModule = value as? JsonObject ?: return@runCatching

                jsonModule["State"]?.asBooleanOrNull()?.let { module.state = it }
                jsonModule["KeyBind"]?.asIntOrNull()?.let { module.keyBind = it }
            }.onFailure { LOGGER.warn("[Cfg] module $key skipped: ${it.message}") }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonObject = json {
            for (module in moduleManager) {
                module.name to json {
                    "State" to module.state
                    "KeyBind" to module.keyBind
                }
            }
        }
        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}