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
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.montoyo.mcef.api.IBrowser
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

class NextGenClickGuiScreen : GuiScreen() {

    private var browser: IBrowser? = null
    private var currentUrl = ""
    private var browserCreatedAt = 0L
    private var browserTextureFrames = 0

    /** When true, the GUI is opened in the user's external browser instead of being rendered in-game. */
    private var browserMode = false

    private var openButton = Rect()
    private var copyButton = Rect()

    private var pressedButtonMask = 0
    private var focusApplied = false

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        browserCreatedAt = 0L
        browserTextureFrames = 0
        pressedButtonMask = 0
        focusApplied = false
        currentUrl = NextGenClickGuiServer.start()
        layoutButtons()

        browserMode = ClickGUIModule.nextGenInBrowser
        if (browserMode) {
            openExternally()
            return
        }

        NextGenBrowserRuntime.ensureStarted()
        ensureBrowser()
        browser?.resize(mc.displayWidth, mc.displayHeight)
    }

    private fun layoutButtons() {
        val buttonWidth = 190
        val buttonHeight = 20
        val centerX = width / 2 - buttonWidth / 2
        openButton = Rect(centerX, height / 2 + 18, buttonWidth, buttonHeight)
        copyButton = Rect(centerX, height / 2 + 44, buttonWidth, buttonHeight)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val browser = browser
        if (!browserMode && browser != null) {
            val rendered = if (canDrawBrowserTexture(browser)) {
                runCatching { drawBrowserTexture(browser) }.getOrElse {
                    LOGGER.error("[NextGen] In-game browser draw failed", it)
                    false
                }
            } else {
                false
            }
            if (rendered) {
                return
            }
            drawBrowserLoading()
            return
        }

        drawDefaultBackground()
        val title = if (browserMode) "NextGen ClickGUI - opened in your browser" else "NextGen ClickGUI"
        val status = when {
            browserMode -> "Toggle off \"Open In Browser\" in ClickGUI settings to render in-game."
            else -> NextGenBrowserRuntime.detail
        }
        drawCenteredString(fontRendererObj, title, width / 2, height / 2 - 44, 0xffffff)
        drawCenteredString(fontRendererObj, status, width / 2, height / 2 - 24, 0xd0d0d0)
        drawCenteredString(fontRendererObj, currentUrl, width / 2, height / 2 - 8, 0x8fb3ff)

        drawButton(openButton, "Open in browser", mouseX, mouseY)
        drawButton(copyButton, "Copy URL", mouseX, mouseY)
        drawCenteredString(fontRendererObj, "Press ESC to close", width / 2, copyButton.y + copyButton.h + 8, 0x808080)
    }

    private fun drawButton(rect: Rect, label: String, mouseX: Int, mouseY: Int) {
        val hovered = rect.contains(mouseX, mouseY)
        drawRect(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, if (hovered) 0xC03a3a45.toInt() else 0xC0242429.toInt())
        drawRect(rect.x, rect.y, rect.x + rect.w, rect.y + 1, 0x40ffffff)
        drawCenteredString(fontRendererObj, label, rect.x + rect.w / 2, rect.y + (rect.h - 8) / 2, if (hovered) 0xffffff else 0xd0d0d0)
    }

    private fun drawBrowserLoading() {
        drawCenteredString(fontRendererObj, "NextGen ClickGUI", width / 2, height / 2 - 6, 0xffffff)
        drawCenteredString(fontRendererObj, "Loading...", width / 2, height / 2 + 8, 0xd0d0d0)
    }

    private fun canDrawBrowserTexture(browser: IBrowser): Boolean {
        if (browser.getTextureID() <= 0) {
            return false
        }

        return System.currentTimeMillis() - browserCreatedAt >= BROWSER_WARMUP_MILLIS &&
            browserTextureFrames >= BROWSER_WARMUP_FRAMES
    }

    /** Render the embedded browser's texture with 1.8.9 GL calls. Returns false until the texture exists. */
    private fun drawBrowserTexture(browser: IBrowser): Boolean {
        val textureId = browser.getTextureID()
        if (textureId <= 0) {
            return false
        }

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        // CEF off-screen buffers are premultiplied-alpha BGRA: composite with ONE / ONE_MINUS_SRC_ALPHA.
        GlStateManager.tryBlendFuncSeparate(1, 771, 1, 771)
        GlStateManager.enableTexture2D()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.bindTexture(textureId)

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos(width.toDouble(), 0.0, 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()

        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        return true
    }

    override fun updateScreen() {
        if (browserMode) {
            return
        }

        NextGenBrowserRuntime.pump()

        val current = browser
        if (current == null) {
            ensureBrowser()
        } else {
            NextGenBrowserRuntime.updateBrowser(current)
            if (current.getTextureID() > 0) {
                browserTextureFrames++
                if (!focusApplied) {
                    NextGenBrowserRuntime.focus(current, true)
                    focusApplied = true
                }
            }
        }
    }

    override fun handleInput() {
        if (!browserMode && browser != null) {
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
        if (browserMode || browser != null) {
            return
        }

        val api = NextGenBrowserRuntime.readyApi() ?: return

        browser = runCatching {
            api.createBrowser(currentUrl, true).also {
                browserCreatedAt = System.currentTimeMillis()
                browserTextureFrames = 0
                it.resize(mc.displayWidth, mc.displayHeight)
                it.loadURL(currentUrl)
            }
        }.getOrElse {
            LOGGER.error("[NextGen] Could not create the in-game browser", it)
            null
        }
    }

    /** Open the local web ClickGUI in the user's default browser. */
    private fun openExternally() {
        if (currentUrl.isBlank()) {
            return
        }
        runCatching { MiscUtils.showURL(currentUrl) }
            .onFailure { LOGGER.error("[NextGen] Failed to open external browser for $currentUrl", it) }
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
                browser.injectKeyPressedByKeyCode(keyCode, keyChar, modifiers)
                if (keyChar.code != 0) {
                    browser.injectKeyTyped(keyChar, modifiers)
                }
            } else {
                browser.injectKeyReleasedByKeyCode(keyCode, keyChar, modifiers)
            }
        }
    }

    private fun dispatchMouseToBrowser() {
        while (Mouse.next()) {
            val browser = browser ?: continue
            if (mc.displayWidth == 0 || mc.displayHeight == 0) {
                continue
            }
            // The browser is sized in raw framebuffer pixels, so inject those directly (Y flipped).
            val x = Mouse.getEventX()
            val y = mc.displayHeight - Mouse.getEventY()
            val button = Mouse.getEventButton()
            val pressed = Mouse.getEventButtonState()
            val wheel = Mouse.getEventDWheel()

            when {
                wheel != 0 -> browser.injectMouseWheel(x, y, keyboardModifiers() or pressedButtonMask, 1, wheel)
                button == -1 -> browser.injectMouseMove(x, y, keyboardModifiers() or pressedButtonMask, false)
                else -> {
                    // LWJGL buttons (0=left, 1=right, 2=middle) -> AWT (LEFT=1, MIDDLE=2, RIGHT=3).
                    val awtButton = when (button) {
                        0 -> 1
                        1 -> 3
                        2 -> 2
                        else -> 0
                    }
                    if (awtButton == 0) {
                        continue
                    }
                    // AWT extended button-down masks: BUTTON1=0x400, BUTTON2=0x800, BUTTON3=0x1000.
                    val downMask = when (awtButton) {
                        1 -> 0x400
                        2 -> 0x800
                        else -> 0x1000
                    }
                    if (pressed) {
                        if (!focusApplied || awtButton == 1) {
                            NextGenBrowserRuntime.focus(browser, true)
                            focusApplied = true
                        }
                        pressedButtonMask = pressedButtonMask or downMask
                        browser.injectMouseButton(x, y, keyboardModifiers() or pressedButtonMask, awtButton, true, 1)
                    } else {
                        pressedButtonMask = pressedButtonMask and downMask.inv()
                        browser.injectMouseButton(x, y, keyboardModifiers() or pressedButtonMask, awtButton, false, 1)
                    }
                }
            }
        }
    }

    private fun keyboardModifiers(): Int {
        var modifiers = 0
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) modifiers = modifiers or 0x40
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) modifiers = modifiers or 0x80
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) modifiers = modifiers or 0x200
        return modifiers
    }

    private data class Rect(val x: Int = 0, val y: Int = 0, val w: Int = 0, val h: Int = 0) {
        fun contains(px: Int, py: Int) = px >= x && px <= x + w && py >= y && py <= y + h
    }

    private companion object {
        private const val BROWSER_WARMUP_MILLIS = 450L
        private const val BROWSER_WARMUP_FRAMES = 3
    }
}
