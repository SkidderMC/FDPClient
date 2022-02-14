package net.ccbluex.liquidbounce.ui.cef.view

import net.ccbluex.liquidbounce.ui.cef.CefRenderManager
import net.ccbluex.liquidbounce.ui.cef.page.Page
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatAllowedCharacters
import org.cef.browser.CefBrowserCustom
import org.cef.browser.ICefRenderer
import org.cef.browser.lwjgl.CefRendererLwjgl
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

abstract class GuiView(private val page: Page) : GuiScreen() {

    private var cefBrowser: CefBrowserCustom? = null
    private var cefRenderer: ICefRenderer? = null

    // we need to store char data for key release event
    private val pressedKeyMap = mutableMapOf<Int, Char>()

    fun init() {
        if(cefBrowser != null || cefRenderer != null) {
            destroy()
        }
        cefRenderer = CefRendererLwjgl(true)
        cefBrowser = CefBrowserCustom(CefRenderManager.cefClient, page.url, true, null, cefRenderer)
        cefBrowser!!.setCloseAllowed()
        cefBrowser!!.createImmediately()
        cefBrowser!!.setFocus(true)
        cefBrowser!!.wasResized_(Display.getWidth(), Display.getHeight())
        Keyboard.enableRepeatEvents(true)
    }

    fun destroy() {
        cefBrowser?.close(true)
        Keyboard.enableRepeatEvents(false)
        cefBrowser = null
        cefRenderer = null
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // mouse stroll
        if (Mouse.hasWheel()) {
            val wheel = Mouse.getDWheel()
            if (wheel != 0) {
                cefBrowser!!.mouseScrolled(Mouse.getX(), Display.getHeight() - Mouse.getY(), keyModifiers(0), 1, wheel)
            }
        }

        // mouse move
        cefBrowser!!.mouseMoved(Mouse.getX(), Display.getHeight() - Mouse.getY(), 0)

        // key up
        pressedKeyMap.map { it }.forEach { (key, char) ->
            if (!Keyboard.isKeyDown(key)) {
                cefBrowser!!.keyEventByKeyCode(key, char, keyModifiers(0), false)
                pressedKeyMap.remove(key)
            }
        }

        GlStateManager.disableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        cefRenderer!!.render(0.0, 0.0, width.toDouble(), height.toDouble())
        GlStateManager.enableDepth()
    }

    override fun onResize(p_onResize_1_: Minecraft?, p_onResize_2_: Int, p_onResize_3_: Int) {
        cefBrowser!!.wasResized_(Display.getWidth(), Display.getHeight())
        super.onResize(p_onResize_1_, p_onResize_2_, p_onResize_3_)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, key: Int) {
        cefBrowser!!.mouseInteracted(Mouse.getX(), Display.getHeight() - Mouse.getY(), mouseModifiers(keyModifiers(0)), key, true, 1)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, key: Int) {
        cefBrowser!!.mouseInteracted(Mouse.getX(), Display.getHeight() - Mouse.getY(), mouseModifiers(keyModifiers(0)), key, false, 1)
    }

    override fun handleKeyboardInput() {
        if (Keyboard.getEventKeyState()) {
            val char = Keyboard.getEventCharacter()
            val key = Keyboard.getEventKey()
            val mod = keyModifiers(0)
            cefBrowser!!.keyEventByKeyCode(key, char, mod, true)
            pressedKeyMap[key] = char
            if (ChatAllowedCharacters.isAllowedCharacter(char)) {
                cefBrowser!!.keyTyped(char, mod)
            }
            keyTyped(char, key) // this need to be handled to make window closeable
        }

        mc.dispatchKeypresses()
    }

    override fun doesGuiPauseGame() = false

    companion object {
        fun keyModifiers(mod: Int): Int {
            var n = mod
            if (isCtrlKeyDown()) {
                n = n or 0x80
            }
            if (isShiftKeyDown()) {
                n = n or 0x40
            }
            if (isAltKeyDown()) {
                n = n or 0x200
            }
            return n
        }

        fun mouseModifiers(mod: Int): Int {
            var n = mod
            if (Mouse.isButtonDown(0)) {
                n = n or 0x400
            }
            if (Mouse.isButtonDown(2)) {
                n = n or 0x800
            }
            if (Mouse.isButtonDown(1)) {
                n = n or 0x1000
            }
            return n
        }
    }
}