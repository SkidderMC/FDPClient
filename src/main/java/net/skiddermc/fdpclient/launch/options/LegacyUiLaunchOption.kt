package net.skiddermc.fdpclient.launch.options

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.launch.EnumLaunchFilter
import net.skiddermc.fdpclient.launch.LaunchFilterInfo
import net.skiddermc.fdpclient.launch.LaunchOption
import net.skiddermc.fdpclient.launch.data.modernui.ClickGUIModule
import net.skiddermc.fdpclient.launch.data.modernui.ClickGuiConfig
import net.skiddermc.fdpclient.launch.data.modernui.GuiMainMenu
import net.skiddermc.fdpclient.launch.data.modernui.clickgui.ClickGui
import java.io.File

@LaunchFilterInfo([EnumLaunchFilter.LEGACY_UI])
object modernuiLaunchOption : LaunchOption() {

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