package net.ccbluex.liquidbounce.launch.options

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.ccbluex.liquidbounce.launch.LaunchFilterInfo
import net.ccbluex.liquidbounce.launch.LaunchOption
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGuiConfig
import net.ccbluex.liquidbounce.launch.data.legacyui.GuiMainMenu
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.ClickGui
import java.io.File

@LaunchFilterInfo([EnumLaunchFilter.LEGACY_UI])
object LegacyUiLaunchOption : LaunchOption() {

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