/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Constructor of config
 *
 * @param file of config
 */
abstract class FileConfig(val file: File) {

    /**
     * Load config from file
     */
    abstract fun loadConfig(config: String)

    /**
     * Save config to file
     */
    abstract fun saveConfig(): String

    /**
     * Create config
     */
    fun createConfig() {
        file.createNewFile()
    }

    /**
     * Load config file
     */
    fun loadConfigFile(): String {
        return Files.readAllBytes(file.toPath()).toString(StandardCharsets.UTF_8)
    }

    /**
     * Save config file
     */
    fun saveConfigFile(config: String) {
        Files.write(file.toPath(), config.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * @return config file exist
     */
    fun hasConfig(): Boolean {
        return file.exists()
    }
}