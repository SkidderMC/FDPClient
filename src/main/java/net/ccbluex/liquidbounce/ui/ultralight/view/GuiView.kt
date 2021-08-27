package net.ccbluex.liquidbounce.ui.ultralight.view

import com.labymedia.ultralight.input.*
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.support.UltralightUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

abstract class GuiView(private val page: Page) : GuiScreen() {
    lateinit var view: View

    private var factor=1

    fun init(){
        view=View(Display.getWidth(), Display.getHeight())
        view.loadPage(page)
        UltralightEngine.registerView(view)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var resized=false
        if(view.width!=Display.getWidth()) {
            resized=true
        }
        if(view.height!=Display.getHeight()) {
            resized=true
        }
        val sr=ScaledResolution(Minecraft.getMinecraft())
        view.realWidth=sr.scaledWidth
        view.realHeight=sr.scaledHeight
        factor=sr.scaleFactor
        if(resized){
            view.resize(Display.getWidth(), Display.getHeight())
        }

        // mouse stroll
        if(Mouse.hasWheel()){
            val wheel = Mouse.getDWheel()
            if(wheel!=0) {
                view.fireScrollEvent(UltralightScrollEvent()
                    .deltaX(0)
                    .deltaY(wheel)
                    .type(UltralightScrollEventType.BY_PIXEL))
            }
        }

        // mouse move
        view.fireMouseEvent(UltralightMouseEvent()
            .type(UltralightMouseEventType.MOVED)
            .x(mouseX*factor)
            .y(mouseY*factor)
            .button(UltralightMouseEventButton.LEFT))

        view.render()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, key: Int) {
        val button=UltralightUtils.getButtonByButtonID(key)
        button ?: return
        view.fireMouseEvent(UltralightMouseEvent()
            .type(UltralightMouseEventType.DOWN)
            .x(mouseX*factor)
            .y(mouseY*factor)
            .button(button))
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, key: Int) {
        val button=UltralightUtils.getButtonByButtonID(key)
        button ?: return
        view.fireMouseEvent(UltralightMouseEvent()
            .type(UltralightMouseEventType.UP)
            .x(mouseX*factor)
            .y(mouseY*factor)
            .button(button))
    }

    // TODO: Add key event handle
//    override fun handleKeyboardInput() {
//        if (Keyboard.getEventKeyState()) {
//
//            keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())
//        }
//
//        mc.dispatchKeypresses()
//    }

    fun destroy() {
        UltralightEngine.unregisterView(view)
    }

    override fun doesGuiPauseGame() = false
}