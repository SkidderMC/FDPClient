package net.ccbluex.liquidbounce.ui.client.keybind

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.other.PopUI
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * @author liulihaocai
 * FDPClient
 */
class KeyBindManager : GuiScreen() {
    private val baseHeight = 205
    private val baseWidth = 500

    private val keys = ArrayList<KeyInfo>()
    var nowDisplayKey: KeyInfo? = null
    var popUI: PopUI? = null

    override fun initGui() {
        nowDisplayKey = null
        popUI = null
        updateAllKeys()
    }

    fun updateAllKeys() {
        // use async because this may a bit slow
        Thread {
            for (key in keys) {
                key.update()
            }
        }.start()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val mcWidth = ((width * 0.8f) - (width * 0.2f)).toInt()

        GL11.glPushMatrix()

//        GL11.glPushMatrix()
        GL11.glScalef(2f, 2f, 2f)
        FontLoaders.C18.DisplayFonts("%ui.keybind.title%", width * 0.21f * 0.5f, height * 0.2f * 0.5f, Color.WHITE.rgb, FontLoaders.C18)
//        GL11.glPopMatrix()
        GL11.glScalef(0.5f, 0.5f, 0.5f)

        GL11.glTranslatef(width * 0.2f, height * 0.2f + FontLoaders.C18.height * 2.3f, 0F)
        val scale = mcWidth / baseWidth.toFloat()
        // It's easier to use scale
        GL11.glScalef(scale, scale, scale)

        RenderUtils.drawRect(0F, 0F, baseWidth.toFloat(), baseHeight.toFloat(), Color.WHITE.rgb)

        for (key in keys) {
            key.render()
        }

        // render key tab
        if (nowDisplayKey != null) {
            nowDisplayKey!!.renderTab()
        }

        GL11.glPopMatrix()

        // mouse wheel sliding
        if (Mouse.hasWheel()) {
            val wheel = Mouse.getDWheel()
            if (wheel != 0) { // Not scroll wheel, just normal mouse movement
                if (popUI != null) {
                    popUI!!.onStroll(width, height, mouseX, mouseY, wheel)
                } else if (nowDisplayKey != null) {
                    val scaledMouseX = (mouseX - width * 0.2f) / scale
                    val scaledMouseY = (mouseY - (height * 0.2f + FontLoaders.C18.height * 2.3f)) / scale

                    nowDisplayKey!!.stroll(scaledMouseX, scaledMouseY, wheel)
                }
            }
        }

        // Rendering function selection popup
        popUI?.onRender(width, height)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (popUI == null) {
            val scale = ((width * 0.8f) - (width * 0.2f)) / baseWidth
            val scaledMouseX = (mouseX - width * 0.2f) / scale
            val scaledMouseY = (mouseY - (height * 0.2f + FontLoaders.C18.height * 2.3f)) / scale

            if (nowDisplayKey == null) {
                // click out of area
                if (scaledMouseX < 0 || scaledMouseY < 0 || scaledMouseX > baseWidth || scaledMouseY > baseHeight) {
                    mc.displayGuiScreen(null)
                    return
                }

                // process click
                for (key in keys) {
                    if (scaledMouseX > key.posX && scaledMouseY > key.posY &&
                        scaledMouseX < (key.posX + key.width) && scaledMouseY < (key.posY + key.height)
                    ) {
                        key.click(scaledMouseX, scaledMouseY)
                        break
                    }
                }
            } else {
                nowDisplayKey!!.click(scaledMouseX, scaledMouseY)
            }
        } else {
            popUI!!.onClick(width, height, mouseX, mouseY)
        }
    }

    override fun onGuiClosed() {
        // save keybind data
        LiquidBounce.configManager.smartSave()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            if (popUI != null) {
                popUI = null
            } else if (nowDisplayKey != null) {
                nowDisplayKey = null
            } else {
                mc.displayGuiScreen(null)
            }
            return
        }

        popUI?.onKey(typedChar, keyCode)
    }

    init {
        // line1
        keys.add(KeyInfo(12F, 12F, 27F, 32F, Keyboard.KEY_GRAVE, "`"))
        keys.add(KeyInfo(12F + 32F * 1, 12F, 27F, 32F, Keyboard.KEY_1, "1"))
        keys.add(KeyInfo(12F + 32F * 2, 12F, 27F, 32F, Keyboard.KEY_2, "2"))
        keys.add(KeyInfo(12F + 32F * 3, 12F, 27F, 32F, Keyboard.KEY_3, "3"))
        keys.add(KeyInfo(12F + 32F * 4, 12F, 27F, 32F, Keyboard.KEY_4, "4"))
        keys.add(KeyInfo(12F + 32F * 5, 12F, 27F, 32F, Keyboard.KEY_5, "5"))
        keys.add(KeyInfo(12F + 32F * 6, 12F, 27F, 32F, Keyboard.KEY_6, "6"))
        keys.add(KeyInfo(12F + 32F * 7, 12F, 27F, 32F, Keyboard.KEY_7, "7"))
        keys.add(KeyInfo(12F + 32F * 8, 12F, 27F, 32F, Keyboard.KEY_8, "8"))
        keys.add(KeyInfo(12F + 32F * 9, 12F, 27F, 32F, Keyboard.KEY_9, "9"))
        keys.add(KeyInfo(12F + 32F * 10, 12F, 27F, 32F, Keyboard.KEY_0, "0"))
        keys.add(KeyInfo(12F + 32F * 11, 12F, 27F, 32F, Keyboard.KEY_MINUS, "-"))
        keys.add(KeyInfo(12F + 32F * 12, 12F, 27F, 32F, Keyboard.KEY_EQUALS, "="))
        keys.add(KeyInfo(12F + 32F * 13, 12F, 27F + 32F, 32F, Keyboard.KEY_BACK, "Backspace"))
        // line2
        keys.add(KeyInfo(12F, 12F + 37F * 1, 32F * 1.5F - 5F, 32F, Keyboard.KEY_TAB, "Tab"))
        keys.add(KeyInfo(12F + 32F * 1.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_Q, "Q"))
        keys.add(KeyInfo(12F + 32F * 2.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_W, "W"))
        keys.add(KeyInfo(12F + 32F * 3.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_E, "E"))
        keys.add(KeyInfo(12F + 32F * 4.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_R, "R"))
        keys.add(KeyInfo(12F + 32F * 5.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_T, "T"))
        keys.add(KeyInfo(12F + 32F * 6.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_Y, "Y"))
        keys.add(KeyInfo(12F + 32F * 7.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_U, "U"))
        keys.add(KeyInfo(12F + 32F * 8.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_I, "I"))
        keys.add(KeyInfo(12F + 32F * 9.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_O, "O"))
        keys.add(KeyInfo(12F + 32F * 10.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_P, "P"))
        keys.add(KeyInfo(12F + 32F * 11.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_LBRACKET, "["))
        keys.add(KeyInfo(12F + 32F * 12.5F, 12F + 37F * 1, 27F, 32F, Keyboard.KEY_RBRACKET, "]"))
        keys.add(KeyInfo(12F + 32F * 13.5F, 12F + 37F * 1, 32F * 1.5F - 5F, 32F, Keyboard.KEY_BACKSLASH, "\\"))
        // line3
        keys.add(KeyInfo(12F, 12F + 37F * 2, 32F * 2F - 5F, 32F, Keyboard.KEY_TAB, "Caps Lock"))
        keys.add(KeyInfo(12F + 32F * 2F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_A, "A"))
        keys.add(KeyInfo(12F + 32F * 3F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_S, "S"))
        keys.add(KeyInfo(12F + 32F * 4F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_D, "D"))
        keys.add(KeyInfo(12F + 32F * 5F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_F, "F"))
        keys.add(KeyInfo(12F + 32F * 6F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_G, "G"))
        keys.add(KeyInfo(12F + 32F * 7F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_H, "H"))
        keys.add(KeyInfo(12F + 32F * 8F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_J, "J"))
        keys.add(KeyInfo(12F + 32F * 9F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_K, "K"))
        keys.add(KeyInfo(12F + 32F * 10F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_L, "L"))
        keys.add(KeyInfo(12F + 32F * 11F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_SEMICOLON, ";"))
        keys.add(KeyInfo(12F + 32F * 12F, 12F + 37F * 2, 27F, 32F, Keyboard.KEY_APOSTROPHE, "'"))
        keys.add(KeyInfo(12F + 32F * 13F, 12F + 37F * 2, 27F + 32F, 32F, Keyboard.KEY_RETURN, "Enter"))
        // line4
        keys.add(KeyInfo(12F, 12F + 37F * 3, 32F * 2.5F - 5F, 32F, Keyboard.KEY_LSHIFT, "Shift", "LShift"))
        keys.add(KeyInfo(12F + 32F * 2.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_Z, "Z"))
        keys.add(KeyInfo(12F + 32F * 3.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_X, "X"))
        keys.add(KeyInfo(12F + 32F * 4.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_C, "C"))
        keys.add(KeyInfo(12F + 32F * 5.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_V, "V"))
        keys.add(KeyInfo(12F + 32F * 6.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_B, "B"))
        keys.add(KeyInfo(12F + 32F * 7.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_N, "N"))
        keys.add(KeyInfo(12F + 32F * 8.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_M, "M"))
        keys.add(KeyInfo(12F + 32F * 9.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_COMMA, ","))
        keys.add(KeyInfo(12F + 32F * 10.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_PERIOD, "."))
        keys.add(KeyInfo(12F + 32F * 11.5F, 12F + 37F * 3, 27F, 32F, Keyboard.KEY_SLASH, "/"))
        keys.add(KeyInfo(12F + 32F * 12.5F, 12F + 37F * 3, 32F * 2.5F - 5F, 32F, Keyboard.KEY_RSHIFT, "Shift", "RShift"))
        // line5
        keys.add(KeyInfo(12F, 12F + 37F * 4, 32F * 1.5F - 5F, 32F, Keyboard.KEY_LCONTROL, "Ctrl", "LCtrl"))
        keys.add(KeyInfo(12F + 32F * 1.5F, 12F + 37F * 4, 32F * 1.5F - 5F, 32F, Keyboard.KEY_LMENU, "Alt", "LAlt"))
        keys.add(KeyInfo(12F + 32F * 3F, 12F + 37F * 4, 32 * 8F - 5F, 32F, Keyboard.KEY_SPACE, " ", "Space"))
        keys.add(KeyInfo(12F + 32F * 11F, 12F + 37F * 4, 32F * 1.5F - 5F, 32F, Keyboard.KEY_RMENU, "Alt", "RAlt"))
        keys.add(KeyInfo(12F + 32F * 12.5F, 12F + 37F * 4, 27F, 32F, Keyboard.KEY_HOME, "\u00d8", "Home"))
        keys.add(KeyInfo(12F + 32F * 13.5F, 12F + 37F * 4, 32F * 1.5F - 5F, 32F, Keyboard.KEY_RCONTROL, "Ctrl", "RCtrl"))
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}