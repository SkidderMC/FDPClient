package net.ccbluex.liquidbounce.launch.options

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.ccbluex.liquidbounce.launch.LaunchFilterInfo
import net.ccbluex.liquidbounce.launch.LaunchOption
import net.ccbluex.liquidbounce.launch.data.fancyui.BrowseCommand
import net.ccbluex.liquidbounce.launch.data.fancyui.ClickGUIModule
import net.ccbluex.liquidbounce.launch.data.fancyui.GuiPrepare
import net.ccbluex.liquidbounce.ui.cef.CefRenderManager
import net.ccbluex.liquidbounce.ui.cef.window.WindowView
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import org.lwjgl.input.Mouse
import java.awt.Color

@LaunchFilterInfo([EnumLaunchFilter.FANCY_UI])
object FancyUiLaunchOption : LaunchOption() {

    private var started = false
    private var lastMouse = false

    override fun start() {
        started = true

        // old cache dir will be auto deleted cuz we changed the allowed folder in FileManager
        // start loading
        val progressHandler = GuiPrepare.DynamicProgressHandler()
        CefRenderManager.initializeAsync(progressHandler)

        // display loading screen
        val mc = Minecraft.getMinecraft()
        mc.displayGuiScreen(GuiPrepare(progressHandler) {
            // I think LiquidBounce.mainMenu must be initialized here
            mc.displayGuiScreen(LiquidBounce.mainMenu)
        })

        // todo: add new HTML UI and Java To JS Bridge

        LegacyUiLaunchOption.start()

        LiquidBounce.commandManager.registerCommand(BrowseCommand())
    }

    override fun stop() {
        LegacyUiLaunchOption.stop()
        CefRenderManager.stop()
    }

    val windowList = mutableListOf<WindowView>()

    val hasChatFocus: Boolean
        get() = windowList.any { it.focus }

    fun render(fromChat: Boolean, mouseX: Int = 0, mouseY: Int = 0) {
        if (!started)
            return

        val clicked = !lastMouse && Mouse.getEventButtonState()
        lastMouse = Mouse.getEventButtonState()

        windowList.sortedBy { it.lastFocusUpdate }.forEach {
            it.render(fromChat, mouseX - it.x, mouseY - it.y, fromChat && clicked)
        }

        if (fromChat) {
            val mc = Minecraft.getMinecraft()
            val width = mc.currentScreen.width
            val height = mc.currentScreen.height
            val inArea = RenderUtils.inArea(mouseX, mouseY, intArrayOf(width - 30, height - 30, width - 15, height - 15))
            if(inArea && clicked && Mouse.getEventButton() == 0) {
                windowList.add(WindowView())
            }
            RenderUtils.drawRect(width - 30f, height - 30f, width - 15f, height - 15f, if (inArea) { Color.LIGHT_GRAY } else { Color.GRAY })
            mc.fontRendererObj.drawCenteredString("+", width - 22.5f, height - 22.5f - (mc.fontRendererObj.FONT_HEIGHT * 0.5f), Color.WHITE.rgb)
            return
        } else {
            lastMouse = false
        }
    }

    fun keyTyped(char: Char, key: Int) {
        windowList.filter { it.focus }.maxByOrNull { it.lastFocusUpdate }?.keyTyped(char, key)
    }
}