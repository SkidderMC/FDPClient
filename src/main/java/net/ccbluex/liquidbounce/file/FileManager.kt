/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.macro.Macro
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.file.configs.*
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.*
import javax.imageio.ImageIO

class FileManager : MinecraftInstance() {
    val dir = File(mc.mcDataDir, "FDPCLIENT-1.8")
    val fontsDir = File(dir, "fonts")
    val configsDir = File(dir, "configs")
    val soundsDir = File(dir, "sounds")
    val legacySettingsDir = File(dir, "legacy-settings")
    val capesDir = File(dir, "capes")
    val themesDir = File(dir, "themes")
    val accountsConfig = AccountsConfig(File(dir, "accounts.json"))
    var friendsConfig = FriendsConfig(File(dir, "friends.json"))
    val xrayConfig = XRayConfig(File(dir, "xray-blocks.json"))
    val hudConfig = HudConfig(File(dir, "hud.json"))
    val subscriptsConfig = ScriptConfig(File(dir, "subscripts.json"))
    val specialConfig = SpecialConfig(File(dir, "special.json"))
    val backgroundFile = File(dir, "userbackground.png")

    /**
     * Setup everything important
     */
    init {
        setupFolder()
        loadBackground()
        // TODO: delete legacy caches due to use vector font renderer
    }

    /**
     * Setup folder
     */
    fun setupFolder() {
        if (!dir.exists()) {
            dir.mkdir()
        }

        if (!fontsDir.exists()) {
            fontsDir.mkdir()
        }

        if (!configsDir.exists()) {
            configsDir.mkdir()
        }

        if (!soundsDir.exists()) {
            soundsDir.mkdir()
        }

        if (!capesDir.exists()) {
            capesDir.mkdir()
        }

        if (!themesDir.exists()) {
            themesDir.mkdir();
        }

    }

    /**
     * Load all configs in file manager
     */
    fun loadAllConfigs() {
        for (field in javaClass.declaredFields) {
            if (field.type == FileConfig::class.java) {
                try {
                    if (!field.isAccessible) field.isAccessible = true
                    val fileConfig = field[this] as FileConfig
                    loadConfig(fileConfig)
                } catch (e: IllegalAccessException) {
                    ClientUtils.logError("Failed to load config file of field " + field.name + ".", e)
                }
            }
        }
    }

    /**
     * Load a list of configs
     *
     * @param configs list
     */
    fun loadConfigs(vararg configs: FileConfig) {
        for (fileConfig in configs)
            loadConfig(fileConfig)
    }

    /**
     * Load one config
     *
     * @param config to load
     */
    fun loadConfig(config: FileConfig) {
        if (!config.hasConfig()) {
            ClientUtils.logInfo("[FileManager] Skipped loading config: " + config.file.name + ".")
            saveConfig(config, true)
            return
        }
        try {
            config.loadConfig(config.loadConfigFile())
            ClientUtils.logInfo("[FileManager] Loaded config: " + config.file.name + ".")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to load config file: " + config.file.name + ".", t)
        }
    }

    /**
     * Save all configs in file manager
     */
    fun saveAllConfigs() {
        for (field in javaClass.declaredFields) {
            try {
                field.isAccessible = true
                val obj = field[this]
                if (obj is FileConfig) {
                    saveConfig(obj)
                }
            } catch (e: IllegalAccessException) {
                ClientUtils.logError("[FileManager] Failed to save config file of field " + field.name + ".", e)
            }
        }
    }

    /**
     * Save a list of configs
     *
     * @param configs list
     */
    fun saveConfigs(vararg configs: FileConfig) {
        for (fileConfig in configs) saveConfig(fileConfig)
    }

    /**
     * Save one config
     *
     * @param config to save
     */
    fun saveConfig(config: FileConfig) {
        saveConfig(config, true)
    }

    /**
     * Save one config
     *
     * @param config         to save
     * @param ignoreStarting check starting
     */
    private fun saveConfig(config: FileConfig, ignoreStarting: Boolean) {
        if (!ignoreStarting && FDPClient.isStarting) return
        try {
            if (!config.hasConfig()) config.createConfig()
            config.saveConfigFile(config.saveConfig())
            ClientUtils.logInfo("[FileManager] Saved config: " + config.file.name + ".")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to save config file: " + config.file.name + ".", t)
        }
    }

    /**
     * Load background for background
     */
    fun loadBackground() {
        if (backgroundFile.exists()) {
            try {
                val bufferedImage = ImageIO.read(FileInputStream(backgroundFile)) ?: return
                FDPClient.background = ResourceLocation(FDPClient.CLIENT_NAME.lowercase() + "/background.png")
                mc.textureManager.loadTexture(FDPClient.background, DynamicTexture(bufferedImage))
                ClientUtils.logInfo("[FileManager] Loaded background.")
            } catch (e: Exception) {
                ClientUtils.logError("[FileManager] Failed to load background.", e)
            }
        }
    }

    @Throws(IOException::class)
    fun loadLegacy(): Boolean {
        var modified = false
        val modulesFile = File(dir, "modules.json")
        if (modulesFile.exists()) {
            modified = true
            val fr = FileReader(modulesFile)
            try {
                val jsonElement = JsonParser().parse(BufferedReader(fr))
                for ((key, value) in jsonElement.asJsonObject.entrySet()) {
                    val module = FDPClient.moduleManager.getModule(key)
                    if (module != null) {
                        val jsonModule = value as JsonObject
                        module.state = jsonModule["State"].asBoolean
                        module.keyBind = jsonModule["KeyBind"].asInt
                        if (jsonModule.has("Array")) module.array = jsonModule["Array"].asBoolean
                        if (jsonModule.has("AutoDisable")) module.autoDisable =
                            EnumAutoDisableType.valueOf(jsonModule["AutoDisable"].asString)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + modulesFile.name + " " + modulesFile.delete())
        }

        val valuesFile = File(dir, "values.json")
        if (valuesFile.exists()) {
            modified = true
            val fr = FileReader(valuesFile)
            try {
                val jsonObject = JsonParser().parse(BufferedReader(fr)).asJsonObject
                for ((key, value) in jsonObject.entrySet()) {
                    val module = FDPClient.moduleManager.getModule(key)
                    if (module != null) {
                        val jsonModule = value as JsonObject
                        for (moduleValue in module.values) {
                            val element = jsonModule[moduleValue.name]
                            if (element != null) moduleValue.fromJson(element)
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + valuesFile.name + " " + valuesFile.delete())
        }

        val macrosFile = File(dir, "macros.json")
        if (macrosFile.exists()) {
            modified = true
            val fr = FileReader(macrosFile)
            try {
                val jsonArray = JsonParser().parse(BufferedReader(fr)).asJsonArray
                for (jsonElement in jsonArray) {
                    val macroJson = jsonElement.asJsonObject
                    FDPClient.macroManager.macros
                        .add(Macro(macroJson["key"].asInt, macroJson["command"].asString))
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            try {
                fr.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ClientUtils.logInfo("Deleted Legacy config " + macrosFile.name + " " + macrosFile.delete())
        }

        val shortcutsFile = File(dir, "shortcuts.json")
        if (shortcutsFile.exists()) shortcutsFile.delete()

        return modified
    }

    companion object {
        val PRETTY_GSON = GsonBuilder().setPrettyPrinting().create()
    }
}
