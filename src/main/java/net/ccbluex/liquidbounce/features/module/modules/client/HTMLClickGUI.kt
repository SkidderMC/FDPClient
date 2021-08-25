package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.view.DynamicGuiView
import net.ccbluex.liquidbounce.ui.ultralight.view.Page
import org.lwjgl.input.Keyboard
import java.io.File

@ModuleInfo(name = "HTMLClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_LMENU, canEnable = false)
class HTMLClickGUI : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(DynamicGuiView(Page(File(UltralightEngine.pagesPath,"clickgui.html"))))
    }
}