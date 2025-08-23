/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
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
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.shader.Background
import net.ccbluex.liquidbounce.utils.io.zipFilesTo
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.io.File
import java.io.IOException

private val FILE_CONFIGS = ArrayList<FileConfig>()

@SideOnly(Side.CLIENT)
object FileManager : MinecraftInstance, Iterable<FileConfig> by FILE_CONFIGS {

    private val sections = mutableListOf<ConfigSection>()

    val dir = File(mc.mcDataDir, "FDPCLIENT")
    val fontsDir = File(dir, "fonts")
    val settingsDir = File(dir, "settings")
    val themesDir = File(dir, "themes")
    val pluginsDir = File(dir, "plugins")

    val modulesConfig = +ModulesConfig(File(dir, "modules.json"))
    val valuesConfig = +ValuesConfig(File(dir, "values.json"))
    val clickGuiConfig = +ClickGuiConfig(File(dir, "clickgui.json"))
    val accountsConfig = +AccountsConfig(File(dir, "accounts.json"))
    val friendsConfig = +FriendsConfig(File(dir, "friends.json"))
    val colorThemeConfig = +ColorThemeConfig(File(dir, "colorTheme.json"))
    val hudConfig = +HudConfig(File(dir, "hud.json"))

    val backgroundImageFile = File(dir, "userbackground.png")
    val backgroundShaderFile = File(dir, "userbackground.frag")

    var firstStart = false
        private set

    var backedup = false
        private set

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
     * Register a FileConfig to FileManager
     * @author MukjepScarlet
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun <T : FileConfig> T.unaryPlus(): T = apply {
        FILE_CONFIGS.add(this)
    }

    /**
     * Setup folder
     */
    private fun setupFolder() {
        if (!dir.exists()) {
            dir.mkdir()
        }
        if (!fontsDir.exists()) fontsDir.mkdir()
        if (!settingsDir.exists()) settingsDir.mkdir()
        if (!themesDir.exists()) themesDir.mkdir()
        if (!pluginsDir.exists()) pluginsDir.mkdir()
    }

    /**
     * Backup all configs as a ZIP file.
     * @author MukjepScarlet
     */
    fun backupAllConfigs(previousVersion: String, currentVersion: String) {
        try {
            FILE_CONFIGS.mapNotNull { it.file.takeIf(File::isFile) }.zipFilesTo(File(dir, "backup_${previousVersion}_${currentVersion}.zip"))
            backedup = true
            LOGGER.info("[FileManager] Successfully backed up all configs.")
        } catch (e: Exception) {
            LOGGER.error("[FileManager] Failed backup configs!", e)
        }
    }

    /**
     * Delete a file
     */
    fun deleteFile(file: File): Boolean {
        return file.delete()
    }

    /**
     * Write text to a file
     */
    fun writeFile(file: File, text: String, append: Boolean = false) {
        try {
            file.writer(Charsets.UTF_8).use { writer ->
                if (append) {
                    writer.appendLine(text)
                } else {
                    writer.write(text)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to write to file: ${file.name}", e)
        }
    }
    /**
     * Load all configs in file manager
     */
    fun loadAllConfigs() {
        FILE_CONFIGS.forEach {
            try {
                loadConfig(it)
            } catch (e: Exception) {
                LOGGER.error("[FileManager] Failed to load config file of ${it.file.name}.", e)
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
        FILE_CONFIGS.forEach {
            try {
                saveConfig(it)
            } catch (e: Exception) {
                LOGGER.error("[FileManager] Failed to save config file of ${it.file.name}.", e)
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
        val backgroundFile = when {
            backgroundImageFile.exists() -> backgroundImageFile
            backgroundShaderFile.exists() -> backgroundShaderFile
            else -> null
        }

        if (backgroundFile != null) {
            background = Background.fromFile(backgroundFile)
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