/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.module.ModuleBindAction
import net.ccbluex.liquidbounce.features.module.ModuleBindModifier
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.file.gson.asBooleanOrNull
import net.ccbluex.liquidbounce.file.gson.asIntOrNull
import net.ccbluex.liquidbounce.file.gson.json
import net.ccbluex.liquidbounce.file.gson.jsonArray
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeTextAtomic
import org.lwjgl.input.Keyboard
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

                jsonModule["KeyBind"]?.asIntOrNull()?.let { module.keyBind = it }
                jsonModule["BindAction"]?.asString?.let {
                    module.bindAction = ModuleBindAction.fromDisplayName(it)
                }
                jsonModule["BindModifiers"]?.asJsonArray?.let { modifiers ->
                    module.bindModifiers = modifiers.mapNotNull {
                        ModuleBindModifier.fromDisplayName(runCatching { it.asString }.getOrNull())
                    }.toSet()
                }
                jsonModule["State"]?.asBooleanOrNull()?.let { enabled ->
                    // HOLD/SMART enable state follows the physical key, not the config. Never restore a
                    // bound HOLD/SMART module as enabled (an older config saved mid-hold must not auto-
                    // enable it on launch). Loaded after BindAction/KeyBind so the gate can see them.
                    if (!enabled || module.bindAction == ModuleBindAction.TOGGLE || module.keyBind == Keyboard.KEY_NONE) {
                        module.state = enabled
                    }
                }
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
                    // Don't persist transient HOLD/SMART enable state; it is driven by the held key.
                    "State" to (module.state &&
                        (module.bindAction == ModuleBindAction.TOGGLE || module.keyBind == Keyboard.KEY_NONE))
                    "KeyBind" to module.keyBind
                    "BindAction" to module.bindAction.displayName
                    "BindModifiers" to jsonArray {
                        module.bindModifiers.forEach { +JsonPrimitive(it.displayName) }
                    }
                }
            }
        }
        file.writeTextAtomic(PRETTY_GSON.toJson(jsonObject))
    }
}
