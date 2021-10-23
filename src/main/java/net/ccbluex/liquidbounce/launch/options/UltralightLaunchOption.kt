package net.ccbluex.liquidbounce.launch.options

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.ccbluex.liquidbounce.launch.LaunchFilterInfo
import net.ccbluex.liquidbounce.launch.LaunchOption
import net.ccbluex.liquidbounce.launch.data.ultralight.BrowseCommand
import net.ccbluex.liquidbounce.launch.data.ultralight.ClickGUIModule
import net.ccbluex.liquidbounce.launch.data.ultralight.GuiMainMenu
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine

@LaunchFilterInfo([EnumLaunchFilter.ULTRALIGHT])
class UltralightLaunchOption : LaunchOption() {
    override fun start() {
        UltralightEngine.initResources()
        UltralightEngine.initEngine()

        LiquidBounce.mainMenu = GuiMainMenu

        LiquidBounce.commandManager.registerCommand(BrowseCommand())
        LiquidBounce.moduleManager.registerModule(ClickGUIModule)
    }
}