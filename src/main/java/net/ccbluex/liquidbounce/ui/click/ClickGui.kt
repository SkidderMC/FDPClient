package net.ccbluex.liquidbounce.ui.click

import net.minecraft.client.gui.GuiScreen
import org.lwjgl.opengl.GL11

open class ClickGui : GuiScreen() {
    open val name=""
    open val scale=1F

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GL11.glPushMatrix()

        GL11.glScalef(scale,scale,scale)

        render(width*scale.toInt(), height*scale.toInt(), mouseX, mouseY, partialTicks)

        GL11.glPopMatrix()
    }

    open fun render(width: Int, height: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {}

    open fun load() {}
}