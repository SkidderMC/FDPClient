package net.ccbluex.liquidbounce.launch.data.ultralight

import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.view.DynamicGuiView
import net.ccbluex.liquidbounce.ui.ultralight.view.Page
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.client.GuiModList
import java.io.File

object GuiMainMenu : DynamicGuiView(Page(File(UltralightEngine.pagesPath, "mainmenu.html"))) {
    @JvmField
    var cancelledUpdate = false
    @JvmField
    var updateVersion: String? = null
    @JvmField
    var updateMessage: String? = null

    @JvmStatic
    fun action(code: Int) {
        when (code) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            3 -> mc.displayGuiScreen(GuiBackground(this))
            4 -> mc.displayGuiScreen(GuiModList(this))
            5 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            6 -> mc.shutdown()
        }
    }

    @JvmStatic
    fun mcTranslate(str: String): String {
        return I18n.format(str)
    }
}