/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.background
import net.ccbluex.liquidbounce.FDPClient.isStarting
import net.ccbluex.liquidbounce.file.configs.*
import net.ccbluex.liquidbounce.utils.Background.Companion.createBackground
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.io.File

@SideOnly(Side.CLIENT)
object FileManager : MinecraftInstance() {

    private val sections = mutableListOf<ConfigSection>()

    val dir = File(mc.mcDataDir, "FDPCLIENT")
    val fontsDir = File(dir, "fonts")
    val settingsDir = File(dir, "settings")
    val themesDir = File(dir, "themes")
    val modulesConfig = ModulesConfig(File(dir, "modules.json"))
    val valuesConfig = ValuesConfig(File(dir, "values.json"))
    val clickGuiConfig = ClickGuiConfig(File(dir, "clickgui.json"))
    val accountsConfig = AccountsConfig(File(dir, "accounts.json"))
    val friendsConfig = FriendsConfig(File(dir, "friends.json"))
    val hudConfig = HudConfig(File(dir, "hud.json"))
    val backgroundImageFile = File(dir, "userbackground.png")
    val backgroundShaderFile = File(dir, "userbackground.frag")
    val PRETTY_GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Constructor of file manager
     * Setup everything important
     */
    init {
        setupFolder()
    }

    /**
     * Current config
     */
    var nowConfig = "default"

    /**
     * Setup folder
     */
    fun setupFolder() {
        if (!dir.exists()) {
            dir.mkdir()
        }
        if (!fontsDir.exists()) fontsDir.mkdir()
        if (!settingsDir.exists()) settingsDir.mkdir()
        if (!themesDir.exists()) themesDir.mkdir()
    }

    /**
     * Load all configs in file manager
     */
    fun loadAllConfigs() {
        for (field in javaClass.declaredFields) {
            if (FileConfig::class.java.isAssignableFrom(field.type)) {
                try {
                    if (!field.isAccessible) field.isAccessible = true
                    val fileConfig = field[this] as FileConfig
                    loadConfig(fileConfig)
                } catch (e: IllegalAccessException) {
                    LOGGER.error("Failed to load config file of field ${field.name}.", e)
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
        for (fileConfig in configs) loadConfig(fileConfig)
    }

    /**
     * Load one config
     *
     * @param config to load
     */
    fun loadConfig(config: FileConfig) {
        if (!config.hasConfig()) {
            LOGGER.info("[FileManager] Skipped loading config: ${config.file.name}.")
            config.loadDefault()
            saveConfig(config, false)
            return
        }
        try {
            config.loadConfig()
            LOGGER.info("[FileManager] Loaded config: ${config.file.name}.")
        } catch (t: Throwable) {
            LOGGER.error("[FileManager] Failed to load config file: ${config.file.name}.", t)
        }
    }

    /**
     * Save all configs in file manager
     */
    fun saveAllConfigs() {
        for (field in javaClass.declaredFields) {
            if (FileConfig::class.java.isAssignableFrom(field.type)) {
                try {
                    if (!field.isAccessible) field.isAccessible = true
                    val fileConfig = field[this] as FileConfig
                    saveConfig(fileConfig)
                } catch (e: IllegalAccessException) {
                    LOGGER.error("[FileManager] Failed to save config file of field ${field.name}.", e)
                }
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
     * @param config         to save
     * @param ignoreStarting check starting
     */
    fun saveConfig(config: FileConfig, ignoreStarting: Boolean = true) {
        if (ignoreStarting && isStarting) return

        try {
            if (!config.hasConfig()) config.createConfig()
            config.saveConfig()
            LOGGER.info("[FileManager] Saved config: ${config.file.name}.")
        } catch (t: Throwable) {
            LOGGER.error("[FileManager] Failed to save config file: ${config.file.name}.", t)
        }
    }

    /**
     * Load background for background
     */
    fun loadBackground() {
        var backgroundFile: File? = null
        if (backgroundImageFile.exists()) {
            backgroundFile = backgroundImageFile
        } else if (backgroundShaderFile.exists()) {
            backgroundFile = backgroundShaderFile
        }

        if (backgroundFile != null) {
            background = createBackground(backgroundFile)
        }
    }

    /**
     * Load a specific config
     *
     * @param name the name of the config
     * @param save whether to save the current config before loading the new one
     */
    fun load(name: String, save: Boolean = true) {
        FDPClient.isLoadingConfig = true
        if (save && nowConfig != name) {
            saveAllConfigs() // Save all current configs before loading the new one
        }

        nowConfig = name
        val configFile = File(settingsDir, "$name.json")

        val json = if (configFile.exists()) {
            JsonParser().parse(configFile.reader(Charsets.UTF_8)).asJsonObject
        } else {
            JsonObject()
        }

        for (section in sections) {
            section.load(if (json.has(section.sectionName)) { json.getAsJsonObject(section.sectionName) } else { JsonObject() })
        }

        if (!configFile.exists()) {
            saveAllConfigs() // Save the new config if it doesn't exist
        }

        if (save) {
            saveAllConfigs()
        }

        LOGGER.info("Config $name.json loaded.")
        FDPClient.isLoadingConfig = false
    }
}