package net.ccbluex.liquidbounce.ui.client.clickgui.options

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.clickgui.EnumLaunchFilter
import net.ccbluex.liquidbounce.ui.client.clickgui.LaunchFilterInfo
import net.ccbluex.liquidbounce.ui.client.clickgui.LaunchOption
import net.ccbluex.liquidbounce.ui.client.clickgui.data.modernui.ClickGUIModule
import net.ccbluex.liquidbounce.ui.client.clickgui.data.modernui.ClickGuiConfig
import net.ccbluex.liquidbounce.ui.client.clickgui.data.modernui.GuiMainMenu
import net.ccbluex.liquidbounce.ui.client.clickgui.data.modernui.clickgui.ClickGui
import java.io.File

@LaunchFilterInfo([EnumLaunchFilter.MODERN_UI])
object modernuiLaunchOption : LaunchOption() {

    @JvmStatic
    lateinit var clickGui: ClickGui

    @JvmStatic
    lateinit var clickGuiConfig: ClickGuiConfig

    override fun start() {
        LiquidBounce.mainMenu = GuiMainMenu()
        LiquidBounce.moduleManager.registerModule(ClickGUIModule())

        clickGui = ClickGui()
        clickGuiConfig = ClickGuiConfig(File(LiquidBounce.fileManager.dir, "clickgui.json"))
        LiquidBounce.fileManager.loadConfig(clickGuiConfig)
    }

    override fun stop() {
        LiquidBounce.fileManager.saveConfig(clickGuiConfig)
    }
}