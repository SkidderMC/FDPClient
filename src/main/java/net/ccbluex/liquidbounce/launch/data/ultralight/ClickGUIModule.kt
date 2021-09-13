package net.ccbluex.liquidbounce.launch.data.ultralight

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.view.DynamicGuiView
import net.ccbluex.liquidbounce.ui.ultralight.view.Page
import org.lwjgl.input.Keyboard
import java.io.File

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
object ClickGUIModule : Module() {
    @JvmField
    var openCategory: Int = -1 // index in the js array
    @JvmField
    var openModule: Int = -1 // index in the js array

    override fun onEnable() {
        mc.displayGuiScreen(DynamicGuiView(Page(File(UltralightEngine.pagesPath, "clickgui.html"))))
    }
}