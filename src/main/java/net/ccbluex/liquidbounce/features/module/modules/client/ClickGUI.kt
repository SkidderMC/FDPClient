package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.view.AllTimeGuiView
import net.ccbluex.liquidbounce.ui.ultralight.view.DynamicGuiView
import net.ccbluex.liquidbounce.ui.ultralight.view.Page
import org.lwjgl.input.Keyboard
import java.io.File

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
class ClickGUI : Module() {
    private var clickgui=AllTimeGuiView(Page(File(UltralightEngine.pagesPath,"clickgui.html")))
    private var language=LanguageManager.language.locale

    override fun onEnable() {
        if(!language.equals(LanguageManager.language.locale)){
            clickgui.destroy()
            clickgui.init()
            language=LanguageManager.language.locale
        }
        mc.displayGuiScreen(clickgui)
    }
}