package net.ccbluex.liquidbounce.ui.client.gui.newVer.element

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.GuiTextField

class SearchBox(componentId: Int, x: Int, y: Int, width: Int, height: Int): GuiTextField(componentId, Fonts.font40, x, y, width, height) {
    override fun getEnableBackgroundDrawing() = false
}