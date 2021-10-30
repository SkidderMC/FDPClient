/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.io.File

class ScriptManager {

    val scripts = mutableListOf<Script>()

    val scriptsFolder = File(LiquidBounce.fileManager.dir, "scripts")

    /**
     * Loads all scripts inside the scripts folder.
     */
    fun loadScripts() {
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdir()
        }

        scriptsFolder.listFiles().forEach {
            if (it.name.endsWith(".js", true)) {
                loadJsScript(it)
            }
        }
    }

    /**
     * Unloads all scripts.
     */
    fun unloadScripts() {
        scripts.clear()
    }

    /**
     * Loads a script from a file.
     */
    fun loadJsScript(scriptFile: File) {
        try {
            scripts.add(Script(scriptFile))
            ClientUtils.logInfo("[ScriptAPI] Successfully loaded script '${scriptFile.name}'.")
        } catch (t: Throwable) {
            ClientUtils.logError("[ScriptAPI] Failed to load script '${scriptFile.name}'.", t)
        }
    }

    /**
     * Enables all scripts.
     */
    fun enableScripts() {
        scripts.forEach { it.onEnable() }
    }

    /**
     * Disables all scripts.
     */
    fun disableScripts() {
        scripts.forEach { it.onDisable() }
    }
}