/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.handler.api.ClientUpdate.gitInfo
import net.ccbluex.liquidbounce.handler.api.loadSettings
import net.ccbluex.liquidbounce.handler.api.messageOfTheDay
import net.ccbluex.liquidbounce.handler.cape.CapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventManager.callEvent
import net.ccbluex.liquidbounce.event.EventManager.registerListener
import net.ccbluex.liquidbounce.event.StartupEvent
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommands
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.ModuleManager.registerModules
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Tower
import net.ccbluex.liquidbounce.handler.payload.ClientFixes
import net.ccbluex.liquidbounce.handler.discord.DiscordRPC
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.FileManager.loadAllConfigs
import net.ccbluex.liquidbounce.file.FileManager.saveAllConfigs
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.handler.lang.LanguageManager.loadLanguages
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.ScriptManager.enableScripts
import net.ccbluex.liquidbounce.script.ScriptManager.loadScripts
import net.ccbluex.liquidbounce.script.remapper.Remapper
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.handler.tabs.BlocksTab
import net.ccbluex.liquidbounce.handler.tabs.ExploitsTab
import net.ccbluex.liquidbounce.handler.tabs.HeadsTab
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.updateClientWindow
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager.Companion.loadActiveGenerators
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.font.Fonts.loadFonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.ClassUtils.hasForge
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister
import net.ccbluex.liquidbounce.utils.timing.TickedActions
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils

object FDPClient {

    /**
     * Client Information
     *
     * This has all of the basic information.
     */
    const val CLIENT_NAME = "FDPClient"
    const val CLIENT_AUTHOR = "Zywl 1zuna"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    const val CLIENT_WEBSITE = "fdpinfo.github.io"
    const val CLIENT_VERSION = "b4"
    
    val clientVersionText = gitInfo["git.build.version"]?.toString() ?: "unknown"
    val clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "unknown"

    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of regular legacy versioning.
     */
    const val IN_DEV = true

    val clientTitle = CLIENT_NAME + " " + clientVersionText + " " + clientCommit + "  | " + if (IN_DEV) " | DEVELOPMENT BUILD" else ""

    var isStarting = true
    var isLoadingConfig = true

    // Managers
    val moduleManager = ModuleManager
    val commandManager = CommandManager
    val eventManager = EventManager
    val fileManager = FileManager
    val scriptManager = ScriptManager
    private var combatManager = CombatManager

    // HUD & ClickGUI
    val hud = HUD

    val clickGui = ClickGui

    // Menu Background
    var background: Background? = null

    // Discord RPC
    var discordRPC = DiscordRPC

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true
        isLoadingConfig = true

        LOGGER.info("Launching...")

        runBlocking {
            runCatching {
                async {
                    // Load languages
                    loadLanguages()

                    // Register listeners
                    registerListener(RotationUtils)
                    registerListener(ClientFixes)

                    registerListener(CapeService)
                    registerListener(combatManager)
                    registerListener(InventoryUtils)
                    registerListener(MiniMapRegister)
                    registerListener(TickedActions)
                    registerListener(MovementUtils)
                    registerListener(PacketUtils)
                    registerListener(TimerBalanceUtils)
                    registerListener(BPSUtils)
                    registerListener(Tower)
                    registerListener(WaitTickUtils)

                    // Load client fonts
                    loadFonts()

                    // Load settings
                    loadSettings(false) {
                        LOGGER.info("Successfully loaded ${it.size} settings.")
                    }

                    // Register commands
                    registerCommands()

                    // Setup module manager and register modules
                    registerModules()

                    APIConnecter.checkStatus()
                    APIConnecter.checkChangelogs()
                    APIConnecter.checkBugs()
                    APIConnecter.loadPictures()
                    APIConnecter.loadDonors()

                    runCatching {
                        // Remapper
                        loadSrg()

                        if (!Remapper.mappingsLoaded) {
                            error("Failed to load SRG mappings.")
                        }

                        // ScriptManager
                        loadScripts()
                        enableScripts()
                    }.onFailure {
                        LOGGER.error("Failed to load scripts.", it)
                    }

                    // Load configs
                    loadAllConfigs()

                    // Update client window
                    updateClientWindow()

                    // Tabs (Only for Forge!)
                    if (hasForge()) {
                        BlocksTab()
                        ExploitsTab()
                        HeadsTab()
                    }

                    // Disable optifine fastrender
                    disableFastRender()

                    // Load alt generators
                    loadActiveGenerators()

                    // Load message of the day
                    messageOfTheDay?.message?.let { LOGGER.info("Message of the day: $it") }

                    // init discord rpc
                    discordRPC = DiscordRPC

                    // Login into known token if not empty
                    if (CapeService.knownToken.isNotBlank()) {
                        runCatching {
                            CapeService.login(CapeService.knownToken)
                        }.onFailure {
                            LOGGER.error("Failed to login into known cape token.", it)
                        }.onSuccess {
                            LOGGER.info("Successfully logged in into known cape token.")
                        }
                    }

                    // Refresh cape service
                    CapeService.refreshCapeCarriers {
                        LOGGER.info("Successfully loaded ${CapeService.capeCarriers.size} cape carriers.")
                    }
                }.await() // Wait to load

                // Load background
                FileManager.loadBackground()

            }.onFailure {
                LOGGER.error("Failed to start client ${it.message}")
            }.onSuccess {
                // Set is starting status
                isStarting = false

                callEvent(StartupEvent())
                LOGGER.info("Successfully started client")
            }
        }
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        callEvent(ClientShutdownEvent())

        // Save all available configs
        saveAllConfigs()

        // Shutdown discord rpc
        discordRPC.stop()
    }

}