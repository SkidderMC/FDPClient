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
import net.ccbluex.liquidbounce.file.FileManager.settingsDir
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
import java.io.IOException

object SettingsCommand : Command("autosettings", "autosetting", "settings", "setting", "config") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax("$usedAlias <load/loadlocal/list/upload/report/create/delete/openfolder/save/rename/current/copy>")
            return
        }

        GlobalScope.launch {
            when (args[1].lowercase()) {
                "load" -> loadSettings(args)
                "loadlocal" -> localSettings(args)
                "report" -> reportSettings(args)
                "upload" -> uploadSettings(args)
                "list" -> listSettings()
                "create" -> createSettings(args)
                "delete" -> deleteConfig(args)
                "openfolder" -> openFolder()
                "save" -> saveConfig(args)
                "rename" -> renameConfig(args)
                "current" -> currentConfig()
                "copy" -> copyConfig(args)
                else -> chatSyntax("$usedAlias <load/loadlocal/list/upload/report/create/delete/openfolder/save/rename/current/copy>")
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

    private suspend fun localSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} loadlocal <name>")
                return@withContext
            }

            val settingsFile = File(settingsDir, args[2] + ".txt")

            if (!settingsFile.exists()) {
                chat("§cSettings file does not exist! §e(Ensure its .txt)")
                return@withContext
            }

            try {
                chat("§9Loading settings...")
                val settings = settingsFile.readText()
                chat("§9Set settings...")
                SettingsUtils.applyScript(settings)
                chat("§6Settings applied successfully.")
                addNotification(Notification("Updated Settings", "SUCESS", Type.SUCCESS))
                playEdit()
            } catch (e: IOException) {
                e.printStackTrace()
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
            try {
                chat("Loading settings...")
                loadSettings(false) { settingsList ->
                    // List existing settings
                    if (settingsList.isNotEmpty()) {
                        settingsList.forEach { setting ->
                            chat("> ${setting.settingId} (Last updated: ${setting.date}, Status: ${setting.statusType.displayName})")
                        }
                    } else {
                        chat("No internal settings found.")
                    }

                    // List additional config files
                    val configFiles = FDPClient.fileManager.settingsDir.listFiles { _, name ->
                        name.endsWith(".txt")
                    }

                    if (configFiles.isNullOrEmpty()) {
                        chat("No additional configuration files found.")
                    } else {
                        chat("Additional configs:")
                        configFiles.forEach { file ->
                            val configName = file.nameWithoutExtension
                            if (settingsList.none { it.settingId == configName }) {
                                chat("§b> $configName")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to list configs", e)
                chat("Failed to list configs: ${e.message}")
            }
        }
    }


    // Create subcommand
    private suspend fun createSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} create <name>")
                return@withContext
            }

            val settingsFile = File(settingsDir, args[2] + ".txt")

            if (settingsFile.exists()) {
                chat("§cSettings file already exists!")
                return@withContext
            }

            try {
                settingsFile.createNewFile()
                chat("§6Settings file created successfully.")
            } catch (e: IOException) {
                e.printStackTrace()
                chat("§cFailed to create settings file: ${e.message}")
            }
        }
    }

    // Delete subcommand
    private suspend fun deleteConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} delete <configName>")
                return@withContext
            }

            val configName = args[2]

            if (configName.startsWith("http")) {
                chat("§cCannot delete URL configs.")
                return@withContext
            }

            val file = File(FDPClient.fileManager.settingsDir, "$configName.txt")

            if (file.exists()) {
                val success = file.delete()
                if (success) {
                    chat("§9Config '$configName' deleted successfully.")
                } else {
                    chat("§9Failed to delete config '$configName'.")
                }
            } else {
                chat("§9Config '$configName' does not exist.")
            }
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
            if (args.size <= 2) {
                chatSyntax("${args[0].lowercase()} save <name> [all/values/binds/states]...")
                return@withContext
            }

            val settingsFile = File(settingsDir, args[2] + ".txt")

            try {
                if (settingsFile.exists())
                    settingsFile.delete()

                settingsFile.createNewFile()

                val option = if (args.size > 3) StringUtils.toCompleteString(args, 3).lowercase() else "all"
                val all = "all" in option
                val values = all || "values" in option
                val binds = all || "binds" in option
                val states = all || "states" in option

                if (!values && !binds && !states) {
                    chatSyntaxError()
                    return@withContext
                }

                chat("§9Creating settings...")
                val settingsScript = SettingsUtils.generateScript(values, binds, states)

                chat("§9Saving settings...")
                settingsFile.writeText(settingsScript)

                chat("§6Settings saved successfully.")
            } catch (throwable: Throwable) {
                chat("§cFailed to create local config: §3${throwable.message}")
                LOGGER.error("Failed to create local config.", throwable)
            }
        }
    }

    // Rename subcommand
    private suspend fun renameConfig(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 4) {
                chatSyntax("${args[0].lowercase()} rename <configName> <newName>")
                return@withContext
            }

            val oldName = args[2]
            val newName = args[3]

            if (oldName.startsWith("http")) {
                chat("§cCannot rename URL configs.")
                return@withContext
            }

            val oldFile = File(FDPClient.fileManager.settingsDir, "$oldName.txt")
            val newFile = File(FDPClient.fileManager.settingsDir, "$newName.txt")

            if (oldFile.exists()) {
                if (!newFile.exists()) {
                    val success = oldFile.renameTo(newFile)
                    if (success) {
                        chat("§9Config '$oldName' renamed to '$newName' successfully.")
                    } else {
                        chat("§9Failed to rename config '$oldName'.")
                    }
                } else {
                    chat("§9Config '$newName' already exists.")
                }
            } else {
                chat("§9Config '$oldName' does not exist.")
            }
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

            val oldName = args[2]
            val newName = args[3]
            val oldFile = File(FDPClient.fileManager.settingsDir, "$oldName.txt")
            val newFile = File(FDPClient.fileManager.settingsDir, "$newName.txt")

            if (oldFile.exists()) {
                if (!newFile.exists()) {
                    oldFile.copyTo(newFile)
                    chat("§bConfig '$oldName' copied to '$newName' successfully.")
                } else {
                    chat("§bConfig '$newName' already exists.")
                }
            } else {
                chat("§9Config '$oldName' does not exist.")
            }
        }
    }


    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("list", "load", "loadlocal", "upload", "report", "create", "delete", "openfolder", "save", "rename", "current", "copy").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "load", "loadlocal", "report", "create", "delete", "rename", "copy" -> {
                        if (autoSettingsList == null) {
                            loadSettings(true, 500) {}
                        }

                        val configFiles = FDPClient.fileManager.settingsDir.listFiles { _, name ->
                            name.endsWith(".txt")
                        }?.map { it.nameWithoutExtension } ?: emptyList()

                        val allConfigs = autoSettingsList?.map { it.settingId }?.plus(configFiles)?.distinct() ?: configFiles

                        return allConfigs.filter { it.startsWith(args[1], true) }
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
