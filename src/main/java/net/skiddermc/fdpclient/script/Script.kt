package net.skiddermc.fdpclient.script

import jdk.internal.dynalink.beans.StaticClass
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.api.scripting.ScriptUtils
import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.features.command.Command
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.launch.data.legacyui.scriptOnline.Subscriptions
import net.skiddermc.fdpclient.script.api.*
import net.skiddermc.fdpclient.script.api.global.Chat
import net.skiddermc.fdpclient.script.api.global.Setting
import net.skiddermc.fdpclient.utils.ClientUtils
import net.skiddermc.fdpclient.utils.MinecraftInstance
import java.io.File
import java.util.function.Function

class Script(private val scriptFile: File) : MinecraftInstance() {

    private val scriptEngine = NashornScriptEngineFactory().getScriptEngine(
        emptyArray(),
        this.javaClass.classLoader,
        ScriptSafetyManager.classFilter
    )
    var scriptText: String =
        if (!scriptFile.path.contains("CloudLoad")) scriptFile.readText(Charsets.UTF_8) else "//api_version=2"
    var isOnline = false

    // Script information
    lateinit var scriptName: String
    lateinit var scriptVersion: String
    lateinit var scriptAuthors: Array<String>

    private var state = false
    private var isEnable = false
    private val events = HashMap<String, JSObject>()
    private val registeredModules = mutableListOf<Module>()
    private val registeredCommands = mutableListOf<Command>()
    fun getState(): Boolean {
        return isEnable;
    }

    fun getRegisteredModules(): MutableList<Module> {
        return registeredModules;
    }

    init {
        scriptEngine.put("Chat", StaticClass.forClass(Chat::class.java))
        scriptEngine.put("Setting", StaticClass.forClass(Setting::class.java))

        // Global instances
        scriptEngine.put("mc", mc)
        scriptEngine.put("moduleManager", FDPClient.moduleManager)
        scriptEngine.put("commandManager", FDPClient.commandManager)
        scriptEngine.put("scriptManager", FDPClient.scriptManager)

        // Global functions
        scriptEngine.put("registerScript", RegisterScript())
        if (Subscriptions.loadingCloud) {
            scriptText = Subscriptions.tempJs
            isOnline = true
        }
        supportLegacyScripts()

        scriptEngine.eval(scriptText)
        callEvent("load")
    }

    @Suppress("UNCHECKED_CAST")
    inner class RegisterScript : Function<JSObject, Script> {
        /**
         * Global function 'registerScript' which is called to register a script.
         * @param scriptObject JavaScript object containing information about the script.
         * @return The instance of this script.
         */
        override fun apply(scriptObject: JSObject): Script {
            scriptName = scriptObject.getMember("name") as String
            scriptVersion = scriptObject.getMember("version") as String
            scriptAuthors =
                ScriptUtils.convert(scriptObject.getMember("authors"), Array<String>::class.java) as Array<String>

            return this@Script
        }
    }

    /**
     * Registers a new script module.
     * @param moduleObject JavaScript object containing information about the module.
     * @param callback JavaScript function to which the corresponding instance of [ScriptModule] is passed.
     * @see ScriptModule
     */
    @Suppress("unused")
    fun registerModule(moduleObject: JSObject, callback: JSObject) {
        val module = ScriptModule(moduleObject)
        FDPClient.moduleManager.registerModule(module)
        registeredModules += module
        callback.call(moduleObject, module)
    }

    /**
     * Registers a new script command.
     * @param commandObject JavaScript object containing information about the command.
     * @param callback JavaScript function to which the corresponding instance of [ScriptCommand] is passed.
     * @see ScriptCommand
     */
    @Suppress("unused")
    fun registerCommand(commandObject: JSObject, callback: JSObject) {
        val command = ScriptCommand(commandObject)
        FDPClient.commandManager.registerCommand(command)
        registeredCommands += command
        callback.call(commandObject, command)
    }

    fun regAnyThing() {
        registeredModules.forEach { FDPClient.moduleManager.registerModule(it) }
        registeredCommands.forEach { FDPClient.commandManager.registerCommand(it) }
    }

    fun supportLegacyScripts() {
        if (!scriptText.lines().first().contains("api_version=2")) {
            ClientUtils.logWarn("[ScriptAPI] Running script '${scriptFile.name}' with legacy support.")
            val legacyScript =
                FDPClient::class.java.getResource("/assets/minecraft/fdpclient/scriptapi/legacy.js").readText()
            scriptEngine.eval(legacyScript)
        }
    }

    /**
     * Called from inside the script to register a new event handler.
     * @param eventName Name of the event.
     * @param handler JavaScript function used to handle the event.
     */
    fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

    /**
     * Called when the client enables the script.
     */
    fun onEnable() {
        if (state) return

        callEvent("enable")
        state = true
    }

    /**
     * Called when the client disables the script. Handles unregistering all modules and commands
     * created with this script.
     */
    fun onDisable() {
        if (!state) return

        registeredModules.forEach { FDPClient.moduleManager.unregisterModule(it) }
        registeredCommands.forEach { FDPClient.commandManager.unregisterCommand(it) }

        callEvent("disable")
        state = false
    }

    /**
     * Imports another JavaScript file inro the context of this script.
     * @param scriptFile Path to the file to be imported.
     */
    fun import(scriptFile: String) {
        scriptEngine.eval(File(FDPClient.scriptManager.scriptsFolder, scriptFile).readText())
    }

    /**
     * Calls the handler of a registered event.
     * @param eventName Name of the event to be called.
     */
    public fun callEvent(eventName: String) {
        //println("call $eventName")
        if (eventName == "enable") {
            isEnable = true;
        } else {
            if (eventName == "disable") {
                isEnable = false;
            }
        }
        try {
            events[eventName]?.call(null)
        } catch (throwable: Throwable) {
            ClientUtils.logError("[ScriptAPI] Exception in script '$scriptName'!", throwable)
        }
    }
}