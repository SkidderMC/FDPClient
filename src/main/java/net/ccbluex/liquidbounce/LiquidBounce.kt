/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.special.macro.MacroManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.CombatManager
import net.ccbluex.liquidbounce.features.special.ServerSpoof
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.MetricsLite
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindMgr
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.other.MusicManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.misc.betterfps.BetterFPSCore
import net.minecraft.util.ResourceLocation

object LiquidBounce {

    // Client information
    const val CLIENT_NAME = "FDPClient"
    const val CLIENT_VERSION = "v1.2.0"
    const val IN_DEV = false
    const val CLIENT_CREATOR = "Liulihaocai"
    const val MINECRAFT_VERSION = "1.8.9"

    var isStarting = true

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var musicManager: MusicManager
    lateinit var combatManager: CombatManager
    lateinit var macroManager: MacroManager

    // HUD & ClickGUI & KeybindMgr
    lateinit var hud: HUD
    lateinit var clickGui: ClickGui
    lateinit var keyBindMgr: KeyBindMgr

    lateinit var metricsLite: MetricsLite

    // Update information
    var latestVersion = ""
    var displayedUpdateScreen=false

    // Menu Background
    var background: ResourceLocation? = null

    // Better FPS
    lateinit var betterFPSCore: BetterFPSCore

    /***
     * do things that need long time async
     */
    fun initClient(){
        betterFPSCore = BetterFPSCore()
        isStarting = true

        // check update
        if(!IN_DEV) {
            Thread {
                val get = HttpUtils.get("https://fdp.liulihaocai.workers.dev/")
                println(get)
                val jsonObj = JsonParser()
                    .parse(get).asJsonObject
                val version = jsonObj.get("version").asString

                if (!version.equals(CLIENT_VERSION)) {
                    latestVersion = version
                }
            }.start()
        }
    }

    /***
     * Execute if client will be started
     */
    fun startClient() {
        ClientUtils.getLogger().info("Starting $CLIENT_NAME $CLIENT_VERSION, by $CLIENT_CREATOR")

        // Create file manager
        fileManager = FileManager()

        // Crate event manager
        eventManager = EventManager()

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(InventoryUtils())
        eventManager.registerListener(ServerSpoof())

        // Create command manager
        commandManager = CommandManager()

        macroManager = MacroManager()
        eventManager.registerListener(macroManager)

        // Load client fonts
        Fonts.loadFonts()

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()

        // Remapper
        try {
            Remapper.loadSrg()

            // ScriptManager
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        musicManager = MusicManager()

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
                fileManager.friendsConfig, fileManager.xrayConfig, fileManager.shortcutsConfig, fileManager.macrosConfig)

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        // KeyBindManager
        keyBindMgr=KeyBindMgr()

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Disable optifine fastrender
        ClientUtils.disableFastRender()

        // bstats.org user count display
        metricsLite=MetricsLite(11076)

        combatManager=CombatManager()
        eventManager.registerListener(combatManager)

        // Set is starting status
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}