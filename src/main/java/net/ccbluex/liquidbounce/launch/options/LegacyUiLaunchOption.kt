package net.ccbluex.liquidbounce.launch.options

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.ccbluex.liquidbounce.launch.LaunchFilterInfo
import net.ccbluex.liquidbounce.launch.LaunchOption
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGuiConfig
import net.ccbluex.liquidbounce.launch.data.legacyui.GuiMainMenu
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import java.io.File
import javax.swing.JOptionPane

@LaunchFilterInfo([EnumLaunchFilter.LEGACY_UI])
object LegacyUiLaunchOption : LaunchOption() {
    @JvmStatic
    lateinit var clickGui: ClickGui

    @JvmStatic
    lateinit var clickGuiConfig: ClickGuiConfig

    override fun start() {
        // check update
        Thread {
            val get = HttpUtils.get("https://api.github.com/repos/UnlegitMC/FDPClient/releases/latest")

            val jsonObj = JsonParser()
                .parse(get).asJsonObject

            val latestVersion = jsonObj.get("tag_name").asString

            if (LiquidBounce.CLIENT_VERSION == LiquidBounce.CLIENT_REAL_VERSION && !latestVersion.equals(LiquidBounce.CLIENT_VERSION)) {
                val buttons = arrayOf(LanguageManager.get("ui.update.download"), LanguageManager.get("ui.update.dismiss"))
                val selection = JOptionPane.showOptionDialog(null, LanguageManager.getAndFormat("ui.update.released", latestVersion), "Alert",
                    JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[0])
                if (selection == 0) {
                    MiscUtils.showURL("https://${LiquidBounce.CLIENT_WEBSITE}")
                }
            }
        }.start()

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