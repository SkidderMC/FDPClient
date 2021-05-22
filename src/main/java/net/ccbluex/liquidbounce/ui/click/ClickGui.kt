package net.ccbluex.liquidbounce.ui.click

import net.minecraft.client.gui.GuiScreen

class ClickGui : GuiScreen() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}