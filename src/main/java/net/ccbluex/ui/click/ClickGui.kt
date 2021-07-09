package net.ccbluex.liquidbounce.ui.click

import net.minecraft.client.gui.GuiScreen
import org.lwjgl.opengl.GL11

open class ClickGui : GuiScreen() {
    open val name=""
    open val scale=1F

    // mouse record
    private var lastClick=0L
    private var lastSingleClick=0L
    private var clicking=false
    private var clickedX=0
    private var clickedY=0
    private var lastX=0
    private var lastY=0
    private var clickedButton=-1

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scaledMouseX=(mouseX*scale).toInt()
        val scaledMouseY=(mouseY*scale).toInt()

        GL11.glPushMatrix()

        GL11.glScalef(scale,scale,scale)

        render((width*scale).toInt(), (height*scale).toInt(), scaledMouseX, scaledMouseY, partialTicks)

        GL11.glPopMatrix()

        val moveX=scaledMouseX-lastX
        val moveY=scaledMouseY-lastY
        lastX=scaledMouseX
        lastY=scaledMouseY
        if(clicking){
            if(moveX!=0||moveY!=0){
                drag(moveX, moveY, scaledMouseX, scaledMouseY, clickedX, clickedY, clickedButton, System.currentTimeMillis()-lastClick)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val scaledMouseX=(mouseX*scale).toInt()
        val scaledMouseY=(mouseY*scale).toInt()

        lastClick=System.currentTimeMillis()
        clickedX=scaledMouseX
        clickedY=scaledMouseY
        clickedButton=0
        clicking=true
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val scaledMouseX=(mouseX*scale).toInt()
        val scaledMouseY=(mouseY*scale).toInt()

        if((System.currentTimeMillis()-lastClick)<200){
            lastSingleClick = if((System.currentTimeMillis()-lastSingleClick)<700) {
                doubleClick(scaledMouseX,scaledMouseY)
                0L
            }else{
                singleClick(scaledMouseX,scaledMouseY)
                System.currentTimeMillis()
            }
            click(scaledMouseX,scaledMouseY)
        }
        clicking=false
    }

    open fun render(width: Int, height: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {}

    open fun singleClick(mouseX: Int, mouseY: Int) {}

    open fun doubleClick(mouseX: Int, mouseY: Int) {}

    open fun click(mouseX: Int, mouseY: Int) {}

    open fun drag(moveX: Int, moveY: Int, mouseX: Int, mouseY: Int, startX: Int, startY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}

    open fun load() {}
}