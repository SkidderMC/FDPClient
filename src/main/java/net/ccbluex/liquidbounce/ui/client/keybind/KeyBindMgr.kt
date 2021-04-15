package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class KeyBindMgr : GuiScreen() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
//        RenderUtils.drawText("KeyBind Manager", Fonts.fontBold40, (width * 0.3).toInt(), (height * 0.3).toInt(), 3f)
//        RenderUtils.drawRect(width * 0.3f, height * 0.3f + Fonts.fontBold40.height * 1.5f, width * 0.7f, height * 0.7f, Color.WHITE.rgb)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}