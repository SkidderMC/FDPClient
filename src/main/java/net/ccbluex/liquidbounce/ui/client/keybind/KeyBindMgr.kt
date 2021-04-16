package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class KeyBindMgr : GuiScreen() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        RenderUtils.drawText("KeyBind Manager", Fonts.fontBold40, (width * 0.21).toInt(), (height * 0.2).toInt(), 2f)
        RenderUtils.drawRect(width * 0.2f, height * 0.2f + Fonts.fontBold40.height * 2.3f, width * 0.8f, height * 0.8f, Color.WHITE.rgb)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}