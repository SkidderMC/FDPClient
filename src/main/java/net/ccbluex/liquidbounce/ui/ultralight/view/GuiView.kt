package net.ccbluex.liquidbounce.ui.ultralight.view

import com.labymedia.ultralight.input.*
import net.ccbluex.liquidbounce.ui.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.ui.ultralight.support.UltralightUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

abstract class GuiView(private val url: String) : GuiScreen() {
    lateinit var view: View

    fun init(){
        view=View()
        view.loadURL(url)
        UltralightEngine.registerView(view)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
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
            .x(mouseX*UltralightEngine.factor)
            .y(mouseY*UltralightEngine.factor)
            .button(UltralightMouseEventButton.LEFT))

        view.render()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, key: Int) {
        val button=UltralightUtils.getButtonByButtonID(key)
        button ?: return
        view.fireMouseEvent(UltralightMouseEvent()
            .type(UltralightMouseEventType.DOWN)
            .x(mouseX*UltralightEngine.factor)
            .y(mouseY*UltralightEngine.factor)
            .button(button))
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, key: Int) {
        val button=UltralightUtils.getButtonByButtonID(key)
        button ?: return
        view.fireMouseEvent(UltralightMouseEvent()
            .type(UltralightMouseEventType.UP)
            .x(mouseX*UltralightEngine.factor)
            .y(mouseY*UltralightEngine.factor)
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
}