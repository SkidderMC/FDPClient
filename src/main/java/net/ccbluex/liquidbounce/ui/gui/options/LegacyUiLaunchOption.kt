/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.gui.options

import me.zywl.fdpclient.FDPClient
import net.ccbluex.liquidbounce.ui.clickgui.ClickGUIModule
import net.ccbluex.liquidbounce.ui.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.clickgui.ClickGuiConfig
import net.ccbluex.liquidbounce.ui.gui.EnumLaunchFilter
import net.ccbluex.liquidbounce.ui.gui.LaunchFilterInfo
import net.ccbluex.liquidbounce.ui.gui.LaunchOption
import net.ccbluex.liquidbounce.ui.gui.menu.GuiMainMenu
import java.io.File

@LaunchFilterInfo([EnumLaunchFilter.MODERN_UI])
object modernuiLaunchOption : LaunchOption() {

    @JvmStatic
    lateinit var clickGui: ClickGui

    @JvmStatic
    lateinit var clickGuiConfig: ClickGuiConfig

    override fun start() {
        FDPClient.mainMenu = GuiMainMenu()
        FDPClient.moduleManager.registerModule(ClickGUIModule)

        clickGui = ClickGui()
        clickGuiConfig = ClickGuiConfig(
            File(
                FDPClient.fileManager.dir,
                "clickgui.json"
            )
        )
        FDPClient.fileManager.loadConfig(clickGuiConfig)
    }

    override fun stop() {
        FDPClient.fileManager.saveConfig(clickGuiConfig)
    }
}