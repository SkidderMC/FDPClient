/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.handler.api.ClientApi
import net.ccbluex.liquidbounce.handler.api.Status
import net.ccbluex.liquidbounce.handler.api.autoSettingsList
import net.ccbluex.liquidbounce.handler.api.loadSettings
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.SettingsUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

object SettingsCommand : Command("autosettings", "autosetting", "settings", "setting", "config") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax("$usedAlias <load/list/upload/report/create/openfolder/current/copy>")
            return
        }

        GlobalScope.launch {
            when (args[1].lowercase()) {
                "load" -> loadSettings(args)
                "report" -> reportSettings(args)
                "upload" -> uploadSettings(args)
                "list" -> listSettings()
                "create" -> createConfig(args)
                "delete" -> deleteConfig(args)
                "openfolder" -> openFolder()
                "save" -> saveConfig(args)
                "rename" -> renameConfig(args)
                "current" -> currentConfig()
                "copy" -> copyConfig(args)
                else -> chatSyntax("$usedAlias <load/list/upload/report/create/delete/openfolder/save/rename/current/copy>")
            }
        }
    }

    // Load subcommand
    private suspend fun loadSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} load <name/url>")
                return@withContext
            }

            try {
                val settings = if (args[2].startsWith("http")) {
                    val (text, code) = get(args[2])
                    if (code != 200) {
                        error(text)
                    }

                    text
                } else {
                    ClientApi.requestSettingsScript(args[2])
                }

                chat("Applying settings...")
                SettingsUtils.applyScript(settings)
                chat("§6Settings applied successfully")
                addNotification(Notification("Updated Settings", "SUCESS", Type.SUCCESS))
                playEdit()
            } catch (e: Exception) {
                LOGGER.error("Failed to load settings", e)
                chat("Failed to load settings: ${e.message}")
            }
        }
    }

    // Report subcommand
    private suspend fun reportSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} report <name>")
                return@withContext
            }

            try {
                val response = ClientApi.reportSettings(args[2])
                when (response.status) {
                    Status.SUCCESS -> chat("§6${response.message}")
                    Status.ERROR -> chat("§c${response.message}")
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to report settings", e)
                chat("Failed to report settings: ${e.message}")
            }
        }
    }

    // Upload subcommand
    private suspend fun uploadSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            val option = if (args.size > 3) StringUtils.toCompleteString(args, 3).lowercase() else "all"
            val all = "all" in option
            val values = all || "values" in option
            val binds = all || "binds" in option
            val states = all || "states" in option

            if (!values && !binds && !states) {
                chatSyntax("${args[0].lowercase()} upload [all/values/binds/states]...")
                return@withContext
            }

            try {
                chat("§9Creating settings...")
                val settingsScript = SettingsUtils.generateScript(values, binds, states)
                chat("§9Uploading settings...")

                val serverData = mc.currentServerData ?: error("You need to be on a server to upload settings.")

                val name = "${FDPClient.clientCommit}-${serverData.serverIP.replace(".", "_")}"
                val response = ClientApi.uploadSettings(name, mc.session.username, settingsScript)

                when (response.status) {
                    Status.SUCCESS -> {
                        chat("§6${response.message}")
                        chat("§9Token: §6${response.token}")

                        // Store token in clipboard
                        val stringSelection = StringSelection(response.token)
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
                    }
                    Status.ERROR -> chat("§c${response.message}")
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to upload settings", e)
                chat("Failed to upload settings: ${e.message}")
            }
        }
    }

    // List subcommand
    private suspend fun listSettings() {
        withContext(Dispatchers.IO) {
            chat("Loading settings...")
            loadSettings(false) {
                for (setting in it) {
                    chat("> ${setting.settingId} (Last updated: ${setting.date}, Status: ${setting.statusType.displayName})")
                }
            }
        }
    }

    private suspend fun createConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} create <configName>")
                return@withContext
            }
            if (args.size > 2) {
                val file = File(FDPClient.fileManager.settingsDir, "${args[2]}.json")
                if (!file.exists()) {
                    FDPClient.fileManager.load(args[2], true)
                    alert("Created config ${args[2]}")
                } else {
                    alert("Config ${args[2]} already exists")
                }
            } else {
                chatSyntax("${args[1]} <configName>")
            }
            chat("Creating config: ${args[2]}")
        }
    }

    // Delete subcommand
    private suspend fun deleteConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} delete <configName>")
                return@withContext
            }
            // logic
            chat("Deleting config: ${args[2]}")
        }
    }

    // Open folder subcommand
    private suspend fun openFolder() {
        withContext(Dispatchers.IO) {
            Desktop.getDesktop().open(FDPClient.fileManager.settingsDir)
            chat("Opening folder...")
        }
    }

    // Save subcommand
    private suspend fun saveConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            // logic
            chat("Saving current config...")
        }
    }

    // Rename subcommand
    private suspend fun renameConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 4) {
                chatSyntax("${args[0].lowercase()} rename <configName> <newName>")
                return@withContext
            }
            // logic
            chat("Renaming config ${args[2]} to ${args[3]}")
        }
    }

    // Current subcommand
    private suspend fun currentConfig() {
        withContext(Dispatchers.IO) {
            alert("Current config is ${FDPClient.fileManager.nowConfig}")
            chat("Displaying current config...")
        }
    }

    // Copy subcommand
    private suspend fun copyConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 4) {
                chatSyntax("${args[0].lowercase()} copy <configName> <newName>")
                return@withContext
            }
            //  logic
            chat("Copying config ${args[2]} to ${args[3]}")
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("list", "load", "upload", "report", "create", "delete", "openfolder", "save", "rename", "current", "copy").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "load", "report", "create", "delete", "rename", "copy" -> {
                        if (autoSettingsList == null) {
                            loadSettings(true, 500) {}
                        }

                        return autoSettingsList?.filter { it.settingId.startsWith(args[1], true) }?.map { it.settingId }
                            ?: emptyList()
                    }
                    "upload" -> {
                        return listOf("all", "values", "binds", "states").filter { it.startsWith(args[1], true) }
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
