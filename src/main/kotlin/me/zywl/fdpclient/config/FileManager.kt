/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.config

import com.google.gson.*
import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.config.impl.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.handler.macro.Macro
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.*
import javax.imageio.ImageIO

class FileManager : MinecraftInstance() {

    // Directories for storing various resources
    val dir = File(mc.mcDataDir, "FDPCLIENT")
    val fontsDir = File(dir, "fonts")
    val configsDir = File(dir, "configs")
    val soundsDir = File(dir, "sounds")
    val legacySettingsDir = File(dir, "legacy-settings")
    val capesDir = File(dir, "capes")
    val themesDir = File(dir, "themes")

    // Configuration files
    val accountsConfig = AccountsConfig(File(dir, "accounts.json"))
    val friendsConfig = FriendsConfig(File(dir, "friends.json"))
    val xrayConfig = XRayConfig(File(dir, "xray-blocks.json"))
    val hudConfig = HudConfig(File(dir, "hud.json"))
    val specialConfig = SpecialConfig(File(dir, "special.json"))
    val themeConfig = ThemeConfig(File(dir, "themeColor.json"))

    // Background file
    private val backgroundFile = File(dir, "background.png")

    init {
        setupFolder()
        loadBackground()
    }

    /**
     * Setup folders required for the application
     */
    private fun setupFolder() {
        // Create directories if they don't exist
        listOf(dir, fontsDir, configsDir, soundsDir, capesDir, themesDir).forEach { folder ->
            if (!folder.exists()) folder.mkdirs()
        }
    }

    /**
     * Load the background image if it exists
     */
    private fun loadBackground() {
        if (backgroundFile.exists()) {
            try {
                val bufferedImage = ImageIO.read(FileInputStream(backgroundFile)) ?: return
                FDPClient.background = ResourceLocation("fdpclient/gui/design/background.png")
                mc.textureManager.loadTexture(FDPClient.background, DynamicTexture(bufferedImage))
                ClientUtils.logInfo("[FileManager] Loaded background.")
            } catch (e: Exception) {
                ClientUtils.logError("[FileManager] Failed to load background.", e)
            }
        }
    }

    /**
     * Load all configuration files
     */
    fun loadAllConfigs() {
        javaClass.declaredFields.forEach { field ->
            if (field.type == FileConfig::class.java) {
                try {
                    field.isAccessible = true
                    val config = field[this] as FileConfig
                    loadConfig(config)
                } catch (e: IllegalAccessException) {
                    ClientUtils.logError("Failed to load config file of field ${field.name}.", e)
                }
            }
        }
    }

    /**
     * Load specific configuration files
     * @Zywl configs Vararg of FileConfig to load
     */
    fun loadConfigs(vararg configs: FileConfig) {
        configs.forEach { loadConfig(it) }
    }

    /**
     * Load a single configuration file
     * @Zywl config FileConfig to load
     */
    fun loadConfig(config: FileConfig) {
        if (!config.hasConfig()) {
            ClientUtils.logInfo("[FileManager] Skipped loading config: ${config.file.name}.")
            saveConfig(config, true)
            return
        }
        try {
            config.loadConfig(config.loadConfigFile())
            ClientUtils.logInfo("[FileManager] Loaded config: ${config.file.name}.")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to load config file: ${config.file.name}.", t)
        }
    }

    /**
     * Save all configuration files
     */
    fun saveAllConfigs() {
        javaClass.declaredFields.forEach { field ->
            try {
                field.isAccessible = true
                val config = field[this] as? FileConfig
                if (config != null) {
                    saveConfig(config)
                }
            } catch (e: IllegalAccessException) {
                ClientUtils.logError("[FileManager] Failed to save config file of field ${field.name}.", e)
            }
        }
    }

    /**
     * Save specific configuration files
     *  configs Vararg of FileConfig to save
     */
    fun saveConfigs(vararg configs: FileConfig) {
        configs.forEach { saveConfig(it) }
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
     * Save a single configuration file
     *  config FileConfig to save
     *  ignoreStarting Boolean to ignore the starting check
     */
    fun saveConfig(config: FileConfig, ignoreStarting: Boolean = false) {
        if (!ignoreStarting && FDPClient.isStarting) return
        try {
            if (!config.hasConfig()) config.createConfig()
            config.saveConfigFile(config.saveConfig())
            ClientUtils.logInfo("[FileManager] Saved config: ${config.file.name}.")
        } catch (t: Throwable) {
            ClientUtils.logError("[FileManager] Failed to save config file: ${config.file.name}.", t)
        }
    }

    /**
     * Load legacy configurations and convert them to the new format
     * @return Boolean indicating if any legacy configuration was modified
     */
    @Throws(IOException::class)
    fun loadLegacy(): Boolean {
        var modified = false

        // Load modules from legacy file
        modified = loadLegacyFile("modules.json") { jsonElement ->
            jsonElement.asJsonObject.entrySet().forEach { (key, value) ->
                FDPClient.moduleManager.getModule(key)?.let { module ->
                    val jsonModule = value.asJsonObject
                    module.state = jsonModule["State"].asBoolean
                    module.keyBind = jsonModule["KeyBind"].asInt
                    if (jsonModule.has("Array")) module.array = jsonModule["Array"].asBoolean
                    if (jsonModule.has("AutoDisable")) module.autoDisable =
                        EnumAutoDisableType.valueOf(jsonModule["AutoDisable"].asString)
                }
            }
        } || modified

        // Load values from legacy file
        modified = loadLegacyFile("values.json") { jsonElement ->
            jsonElement.asJsonObject.entrySet().forEach { (key, value) ->
                FDPClient.moduleManager.getModule(key)?.let { module ->
                    val jsonModule = value.asJsonObject
                    module.values.forEach { moduleValue ->
                        jsonModule[moduleValue.name]?.let { moduleValue.fromJson(it) }
                    }
                }
            }
        } || modified

        // Load macros from legacy file
        modified = loadLegacyFile("macros.json") { jsonElement ->
            jsonElement.asJsonArray.forEach { element ->
                val macroJson = element.asJsonObject
                FDPClient.macroManager.macros.add(Macro(macroJson["key"].asInt, macroJson["command"].asString))
            }
        } || modified

        // Delete the shortcuts file if it exists
        File(dir, "shortcuts.json").takeIf { it.exists() }?.delete()

        return modified
    }

    /**
     * Helper method to load and parse a legacy file
     * @param fileName String name of the file
     * @param parse Function to parse the JsonElement
     * @return Boolean indicating if the file was successfully loaded and parsed
     */
    private fun loadLegacyFile(fileName: String, parse: (JsonElement) -> Unit): Boolean {
        val file = File(dir, fileName)
        if (file.exists()) {
            FileReader(file).use { fr ->
                try {
                    val jsonElement = JsonParser().parse(BufferedReader(fr))
                    parse(jsonElement)
                    ClientUtils.logInfo("Deleted Legacy config ${file.name} ${file.delete()}")
                    return true
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
        return false
    }

    companion object {
        // Pretty print Gson instance
        val PRETTY_GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    }
}