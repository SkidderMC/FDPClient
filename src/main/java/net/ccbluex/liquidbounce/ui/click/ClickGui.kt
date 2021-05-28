package net.ccbluex.liquidbounce.ui.click

import net.minecraft.client.gui.GuiScreen
import org.lwjgl.opengl.GL11

open class ClickGui : GuiScreen() {
    open val name=""
    open val scale=1F

    // mouse record
    private var lastClick=0L
    private var lastSingleClick=0L
    private var canDrag=false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GL11.glPushMatrix()

        GL11.glScalef(scale,scale,scale)

        render(width*scale.toInt(), height*scale.toInt(), mouseX, mouseY, partialTicks)

        GL11.glPopMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        lastClick=System.currentTimeMillis()
        canDrag=true
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if((System.currentTimeMillis()-lastClick)<200){
            lastSingleClick = if((System.currentTimeMillis()-lastSingleClick)<700) {
                doubleClick(mouseX, mouseY)
                0L
            }else{
                singleClick(mouseX, mouseY)
                System.currentTimeMillis()
            }
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if((System.currentTimeMillis()-lastClick)<200){
            canDrag=false
        }else if(canDrag){
            drag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        }
    }

    open fun render(width: Int, height: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {}

    open fun singleClick(mouseX: Int, mouseY: Int) {}

    open fun doubleClick(mouseX: Int, mouseY: Int) {}

    open fun drag(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}

    open fun load() {}
}