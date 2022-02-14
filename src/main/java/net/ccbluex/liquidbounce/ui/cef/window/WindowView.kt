package net.ccbluex.liquidbounce.ui.cef.window

import net.ccbluex.liquidbounce.launch.options.FancyUiLaunchOption
import net.ccbluex.liquidbounce.ui.cef.CefRenderManager
import net.ccbluex.liquidbounce.ui.cef.view.GuiView
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiTextField
import net.minecraft.util.ChatAllowedCharacters
import org.cef.browser.CefBrowserCustom
import org.cef.browser.ICefRenderer
import org.cef.browser.lwjgl.CefRendererLwjgl
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

class WindowView : MinecraftInstance() {

    var x = 30f
    var y = 30f
    var focus = false
        set(value) {
            field = value
            if(value) {
                lastFocusUpdate = System.currentTimeMillis()
            }
        }
    var width = 140f
    var height = 200f
    var browserScale = 2
    var lastFocusUpdate = System.currentTimeMillis()

    private var handleInput = false
    private var textField = GuiTextField(0, mc.fontRendererObj, 0, 0, 0, 0).also {
        it.maxStringLength = 10000
    }

    private var transparent = false
    private var showTitle = true
    private var drag = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var scale = false

    private val minWidth = 70f
    private val minHeight = 70f

    private val cefBrowser: CefBrowserCustom
    private val cefRenderer: ICefRenderer

    init {
        cefRenderer = CefRendererLwjgl(true)
        cefBrowser = CefBrowserCustom(CefRenderManager.cefClient, "https://yandex.com", true, null, cefRenderer)
        cefBrowser.setCloseAllowed()
        cefBrowser.createImmediately()
        cefBrowser.setFocus(true)
        cefBrowser.wasResized_((width * browserScale).toInt(), (height * browserScale).toInt())
    }

    /**
     * @param mouseX realMouseX - x
     */
    fun render(fromChat: Boolean, mouseX: Float, mouseY: Float, mouseEvent: Boolean) {
        if(drag) {
            x = (mouseX + x) - dragOffsetX
            y = (mouseY + y) - dragOffsetY
            drag = Mouse.isButtonDown(0)
        }
        if(scale) {
            width = mouseX.coerceAtLeast(minWidth)
            height = mouseY.coerceAtLeast(minHeight)
            scale = Mouse.isButtonDown(0)
            mc.fontRendererObj.drawString("${width.toInt()}x${height.toInt()} Scale ${browserScale}", x + width,
                y + height - mc.fontRendererObj.FONT_HEIGHT, Color.WHITE.rgb, false)
            if (Mouse.hasWheel()) {
                val wheel = Mouse.getDWheel()
                if (wheel > 0) {
                    browserScale++
                } else if (wheel < 0) {
                    browserScale--
                }
                browserScale = browserScale.coerceAtLeast(1)
            }
            cefBrowser.wasResized_((width * browserScale).toInt(), (height * browserScale).toInt())
        }

        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, 0f)

        val titleHeight = mc.fontRendererObj.FONT_HEIGHT + 2f

        if(handleInput) {
            if(!fromChat || mouseEvent) {
                textField.text = ""
                handleInput = false
            }
            RenderUtils.drawRect(0f, -titleHeight, width, 0f, Color.LIGHT_GRAY.rgb)
            mc.fontRendererObj.drawString(if(textField.text.isNotEmpty()) { mc.fontRendererObj.trimStringToWidth(textField.text, width.toInt(), true) + if(System.currentTimeMillis() % 1500 > 1000) { "|" } else { "" } } else { "Input URL..." }, 2f, -titleHeight + 1f, Color.WHITE.rgb, false)
        }

        if(showTitle || fromChat) {
            // render title
            RenderUtils.drawRect(0f, 0f, width, titleHeight, if(focus) Color.LIGHT_GRAY else Color.GRAY)
            mc.fontRendererObj.drawString(if(cefBrowser.isLoading) { "Loading..." } else { "Browser" },
                2f, 1f, Color.WHITE.rgb, false)
            mc.fontRendererObj.drawCenteredString("X", width - (titleHeight * 0.5f), 1f, Color.WHITE.rgb)
            if(transparent) {
                RenderUtils.drawRect(width - (titleHeight * 2), 0f, width - titleHeight, titleHeight, Color.WHITE)
            }
            mc.fontRendererObj.drawCenteredString("T", width - (titleHeight * 1.5f), 1f, (if(transparent) Color.BLACK else Color.WHITE).rgb)
            if(!showTitle) {
                RenderUtils.drawRect(width - (titleHeight * 3), 0f, width - (titleHeight * 2), titleHeight, Color.WHITE)
            }
            mc.fontRendererObj.drawCenteredString("S", width - (titleHeight * 2.5f), 1f, (if(!showTitle) Color.BLACK else Color.WHITE).rgb)

            // handle title events
            if(mouseEvent && RenderUtils.inArea(mouseX, mouseY, floatArrayOf(0f, 0f, width, titleHeight))) {
                focus = true
                if(Mouse.getEventButton() == 0) {
                    if(RenderUtils.inArea(mouseX, mouseY, floatArrayOf(width - (titleHeight * 2), 0f, width - titleHeight, titleHeight))) {
                        transparent = !transparent
                    } else if(RenderUtils.inArea(mouseX, mouseY, floatArrayOf(width - (titleHeight * 3), 0f, width - (titleHeight * 2), titleHeight))) {
                        showTitle = !showTitle
                    } else if(RenderUtils.inArea(mouseX, mouseY, floatArrayOf(width - titleHeight, 0f, width, titleHeight))) {
                        finalize()
                    } else {
                        drag = true
                        dragOffsetX = mouseX
                        dragOffsetY = mouseY
                    }
                } else {
                    handleInput = true
                    textField.text = cefBrowser.getURL()
                }
            }
        }
        if (!transparent) {
            RenderUtils.drawRect(0f, titleHeight, width, height, Color.WHITE)
        }
        cefRenderer.render(0.0, titleHeight.toDouble(), width.toDouble(), height.toDouble())
        if(fromChat) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            RenderUtils.glColor(ColorUtils.rainbow()) // background is white, so we need something special

            GL11.glBegin(GL11.GL_POLYGON)
            GL11.glVertex2f(width - 5f, height)
            GL11.glVertex2f(width, height)
            GL11.glVertex2f(width, height - 5f)
            GL11.glEnd()

            GL11.glColor4f(1f, 1f, 1f, 1f)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }

        val mouseRX = (mouseX * browserScale).toInt()
        val mouseRY = ((mouseY - titleHeight) * browserScale).toInt()
        if (RenderUtils.inArea(mouseX, mouseY, floatArrayOf(0f, titleHeight, width, height))) {
            cefBrowser.mouseMoved(mouseRX, mouseRY, 0)
            if (Mouse.hasWheel()) {
                val wheel = Mouse.getDWheel()
                if (wheel != 0) {
                    cefBrowser.mouseScrolled(mouseRX, mouseRY,
                        GuiView.keyModifiers(0), 1, wheel)
                }
            }
        }
        if(mouseEvent && RenderUtils.inArea(mouseX, mouseY, floatArrayOf(0f, titleHeight, width, height))) {
            focus = true
            if(Mouse.getEventButton() == 0 && RenderUtils.inArea(mouseX, mouseY, floatArrayOf(width - 5f, height - 5f, width, height))) {
                scale = true
            } else {
                val mod = GuiView.mouseModifiers(GuiView.keyModifiers(0))
                cefBrowser.mouseInteracted(mouseRX, mouseRY, mod, Mouse.getEventButton(), true, 1)
                cefBrowser.mouseInteracted(mouseRX, mouseRY, mod, Mouse.getEventButton(), false, 1)
            }
        } else if(mouseEvent && !RenderUtils.inArea(mouseX, mouseY, floatArrayOf(0f, 0f, width, titleHeight))) {
            focus = false
        }

        GL11.glPopMatrix()
    }

    fun keyTyped(char: Char, key: Int) {
        if(handleInput) {
            if(key == Keyboard.KEY_RETURN) {
                handleInput = false
                cefBrowser.loadURL(textField.text)
            } else {
                textField.isFocused = true
                textField.textboxKeyTyped(char, key)
            }
        } else {
            val mod = GuiView.keyModifiers(0)
            cefBrowser.keyEventByKeyCode(key, char, mod, true)
            if (ChatAllowedCharacters.isAllowedCharacter(char)) {
                cefBrowser.keyTyped(char, mod)
            }
            cefBrowser.keyEventByKeyCode(key, char, mod, false)
        }
    }

    fun finalize() { // finalize or destroy
        if(FancyUiLaunchOption.windowList.contains(this)) {
            FancyUiLaunchOption.windowList.remove(this)
        }
        cefBrowser.close(true)
    }
}