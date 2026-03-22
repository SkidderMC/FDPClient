package net.ccbluex.liquidbounce.file

import java.io.File

private const val CLIENT_CONFIG_EXTENSION = ".json"
private const val LOCAL_SCRIPT_EXTENSION = ".txt"
private const val YZY_LAYOUT_EXTENSION = ".yzygui"

object SettingsFiles {

    fun clientConfigFile(name: String) = File(FileManager.settingsDir, "$name$CLIENT_CONFIG_EXTENSION")

    fun localScriptFile(name: String) = File(FileManager.settingsDir, "$name$LOCAL_SCRIPT_EXTENSION")

    fun listClientConfigs(): Array<File> =
        FileManager.settingsDir.listFiles { _, fileName -> fileName.endsWith(CLIENT_CONFIG_EXTENSION, ignoreCase = true) }
            ?.sortedBy { it.name.lowercase() }
            ?.toTypedArray()
            ?: emptyArray()

    fun listLocalScripts(): Array<File> =
        FileManager.settingsDir.listFiles { _, fileName -> fileName.endsWith(LOCAL_SCRIPT_EXTENSION, ignoreCase = true) }
            ?.sortedBy { it.name.lowercase() }
            ?.toTypedArray()
            ?: emptyArray()

    fun localScriptName(file: File) = file.name.removeSuffix(LOCAL_SCRIPT_EXTENSION)

    fun yzyLayoutFile(categoryName: String) = File(FileManager.guiLayoutsDir, "$categoryName$YZY_LAYOUT_EXTENSION")

    fun legacyYzyLayoutFile(categoryName: String) = File(FileManager.settingsDir, "$categoryName$YZY_LAYOUT_EXTENSION")
}
