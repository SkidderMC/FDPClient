package net.skiddermc.fdpclient.launch.options

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.launch.EnumLaunchFilter
import net.skiddermc.fdpclient.launch.LaunchFilterInfo
import net.skiddermc.fdpclient.launch.LaunchOption
import net.skiddermc.fdpclient.launch.data.legacyui.ClickGUIModule
import net.skiddermc.fdpclient.launch.data.legacyui.ClickGuiConfig
import net.skiddermc.fdpclient.launch.data.legacyui.GuiMainMenu
import net.skiddermc.fdpclient.launch.data.legacyui.clickgui.ClickGui
import java.io.File

@LaunchFilterInfo([EnumLaunchFilter.LEGACY_UI])
object LegacyUiLaunchOption : LaunchOption() {

    @JvmStatic
    lateinit var clickGui: ClickGui

    @JvmStatic
    lateinit var clickGuiConfig: ClickGuiConfig

    override fun start() {
        FDPClient.mainMenu = GuiMainMenu()
        FDPClient.moduleManager.registerModule(ClickGUIModule())

        clickGui = ClickGui()
        clickGuiConfig = ClickGuiConfig(File(FDPClient.fileManager.dir, "clickgui.json"))
        FDPClient.fileManager.loadConfig(clickGuiConfig)
    }

    override fun stop() {
        FDPClient.fileManager.saveConfig(clickGuiConfig)
    }
}