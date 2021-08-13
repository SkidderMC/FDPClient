/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.script.kotlin.KotlinScript
import net.ccbluex.liquidbounce.script.kotlin.KotlinScriptDependency
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.io.File

class ScriptManager {

    val scripts = mutableListOf<Script>()

    val kotlinScripts = mutableListOf<KotlinScript>()

    val scriptsFolder = File(LiquidBounce.fileManager.dir, "scripts")

    /**
     * Loads all scripts inside the scripts folder.
     */
    fun loadScripts() {
        if(!scriptsFolder.exists())
            scriptsFolder.mkdir()

        scriptsFolder.listFiles().forEach {
            if(it.name.endsWith(".js",true)){
                loadJsScript(it)
            }else if(it.name.endsWith(".kt",true) || it.name.endsWith(".kts",true)){
                loadKtScript(it)
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
    fun loadJsScript(scriptFile : File) {
        try {
            scripts.add(Script(scriptFile))
            ClientUtils.getLogger().info("[ScriptAPI] Successfully loaded script '${scriptFile.name}'.")
        } catch(t : Throwable) {
            ClientUtils.getLogger().error("[ScriptAPI] Failed to load script '${scriptFile.name}'.", t)
        }
    }

    /**
     * Loads a script from a file.
     */
    fun loadKtScript(scriptFile : File) {
        try {
            // prepare scripting runtime
            KotlinScriptDependency.check()
            // load script
            kotlinScripts.add(KotlinScript(scriptFile))
            ClientUtils.getLogger().info("[ScriptAPI] Successfully loaded kotlin script '${scriptFile.name}'.")
        } /*catch (t : NoSuchFieldError){
            JOptionPane.showMessageDialog(null, "If this error first appear, try restart your minecraft." +
                    "and if this error appears many times, try disable \"file complete check\"/\"文件完整性检查\"", "KotlinScript Loaded Failed", JOptionPane.ERROR_MESSAGE)
        } */catch(t : Throwable) {
            ClientUtils.getLogger().error("[ScriptAPI] Failed to load kotlin script '${scriptFile.name}'.", t)
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