/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.montoyo.mcef.api.IBrowser
import net.montoyo.mcef.api.MCEFApi
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class NextGenClickGuiScreen : GuiScreen() {

    private var browser: IBrowser? = null
    private var status = "Starting NextGen ClickGUI..."
    private var currentUrl = ""

    // Fallback (no embedded browser) state
    private var mcefUnavailable = false
    private var openedExternally = false

    private var openButton = Rect()
    private var copyButton = Rect()

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        currentUrl = NextGenClickGuiServer.start()
        layoutButtons()
        ensureBrowser()
        browser?.resize(mc.displayWidth, mc.displayHeight)
    }

    private fun layoutButtons() {
        val buttonWidth = 180
        val buttonHeight = 20
        val centerX = width / 2 - buttonWidth / 2
        openButton = Rect(centerX, height / 2 + 18, buttonWidth, buttonHeight)
        copyButton = Rect(centerX, height / 2 + 44, buttonWidth, buttonHeight)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val browser = browser
        if (browser != null) {
            GlStateManager.disableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.color(1f, 1f, 1f, 1f)
            browser.draw(0.0, height.toDouble(), width.toDouble(), 0.0)
            GlStateManager.enableDepth()
            return
        }

        drawDefaultBackground()
        drawCenteredString(fontRendererObj, "NextGen ClickGUI", width / 2, height / 2 - 44, 0xffffff)
        drawCenteredString(fontRendererObj, status, width / 2, height / 2 - 24, 0xd0d0d0)
        drawCenteredString(fontRendererObj, currentUrl, width / 2, height / 2 - 8, 0x8fb3ff)

        if (mcefUnavailable) {
            drawButton(openButton, "Open in browser", mouseX, mouseY)
            drawButton(copyButton, "Copy URL", mouseX, mouseY)
            drawCenteredString(fontRendererObj, "Press ESC to close", width / 2, copyButton.y + copyButton.h + 8, 0x808080)
        }
    }

    private fun drawButton(rect: Rect, label: String, mouseX: Int, mouseY: Int) {
        val hovered = rect.contains(mouseX, mouseY)
        drawRect(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, if (hovered) 0xC03a3a45.toInt() else 0xC0242429.toInt())
        drawRect(rect.x, rect.y, rect.x + rect.w, rect.y + 1, 0x40ffffff)
        drawCenteredString(fontRendererObj, label, rect.x + rect.w / 2, rect.y + (rect.h - 8) / 2, if (hovered) 0xffffff else 0xd0d0d0)
    }

    override fun updateScreen() {
        if (browser == null) {
            ensureBrowser()
        }
    }

    override fun handleInput() {
        if (browser != null) {
            dispatchKeyboardToBrowser()
            dispatchMouseToBrowser()
            return
        }
        handleFallbackInput()
    }

    private fun handleFallbackInput() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null)
                return
            }
        }

        while (Mouse.next()) {
            if (!Mouse.getEventButtonState() || Mouse.getEventButton() != 0) {
                continue
            }
            if (mc.displayWidth == 0 || mc.displayHeight == 0) {
                continue
            }

            val mouseX = Mouse.getEventX() * width / mc.displayWidth
            val mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1

            when {
                openButton.contains(mouseX, mouseY) -> openExternally()
                copyButton.contains(mouseX, mouseY) -> runCatching { MiscUtils.copy(currentUrl) }
            }
        }
    }

    override fun onGuiClosed() {
        browser?.close()
        browser = null
        Keyboard.enableRepeatEvents(false)
        if (ClickGUIModule.lastScale > 0) {
            mc.gameSettings.guiScale = ClickGUIModule.lastScale
        }
    }

    override fun doesGuiPauseGame() = false

    private fun ensureBrowser() {
        if (browser != null) {
            return
        }

        val api = runCatching { MCEFApi.getAPI() }.getOrNull()
        if (api == null) {
            if (!mcefUnavailable) {
                mcefUnavailable = true
                status = "Embedded browser unavailable - opening in your default browser."
                openExternally()
            }
            return
        }

        browser = runCatching {
            api.createBrowser(currentUrl, false).also {
                it.resize(mc.displayWidth, mc.displayHeight)
            }
        }.onSuccess {
            mcefUnavailable = false
            status = "Embedded browser ready."
        }.getOrElse {
            if (!mcefUnavailable) {
                mcefUnavailable = true
                status = "Could not create embedded browser - opening in your default browser."
                openExternally()
            }
            null
        }
    }

    /** Open the local NextGen ClickGUI server URL in the user's default browser (once, automatically). */
    private fun openExternally() {
        if (currentUrl.isBlank()) {
            return
        }
        runCatching { MiscUtils.showURL(currentUrl) }
            .onFailure { LOGGER.error("[NextGenClickGUI] Failed to open external browser for $currentUrl", it) }
        openedExternally = true
    }

    private fun dispatchKeyboardToBrowser() {
        while (Keyboard.next()) {
            val keyCode = Keyboard.getEventKey()
            val keyChar = Keyboard.getEventCharacter()
            val pressed = Keyboard.getEventKeyState()

            if (pressed && keyCode == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null)
                return
            }

            val browser = browser ?: continue
            val modifiers = keyboardModifiers()

            if (pressed) {
                browser.injectKeyPressed(keyChar, modifiers)
                if (keyChar.code != 0) {
                    browser.injectKeyTyped(keyChar, modifiers)
                }
            } else {
                browser.injectKeyReleased(keyChar, modifiers)
            }
        }
    }

    private fun dispatchMouseToBrowser() {
        while (Mouse.next()) {
            val browser = browser ?: continue
            val x = Mouse.getEventX()
            val y = mc.displayHeight - Mouse.getEventY()
            val button = Mouse.getEventButton()
            val pressed = Mouse.getEventButtonState()
            val wheel = Mouse.getEventDWheel()
            val modifiers = keyboardModifiers()

            when {
                wheel != 0 -> browser.injectMouseWheel(x, y, modifiers, 1, wheel)
                button == -1 -> browser.injectMouseMove(x, y, modifiers, false)
                else -> browser.injectMouseButton(x, y, modifiers, button + 1, pressed, 1)
            }
        }
    }

    private fun keyboardModifiers(): Int = 0

    private data class Rect(val x: Int = 0, val y: Int = 0, val w: Int = 0, val h: Int = 0) {
        fun contains(px: Int, py: Int) = px >= x && px <= x + w && py >= y && py <= y + h
    }
}
