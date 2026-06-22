/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenBrowserRuntime
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenClickGuiServer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.montoyo.mcef.api.IBrowser
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

/**
 * The new Spotify player UI — a standalone web page rendered in its own in-game MCEF browser.
 * It reuses the shared in-game web browser runtime (local server + browser) but is a completely
 * separate screen and page, so it owns its own browser instance pointed at the Spotify player URL.
 */
class SpotifyWebScreen : GuiScreen() {

    private var browser: IBrowser? = null
    private var spotifyUrl = ""
    private var pressedButtonMask = 0
    private var focusApplied = false

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        pressedButtonMask = 0
        focusApplied = false
        NextGenClickGuiServer.start()
        spotifyUrl = NextGenClickGuiServer.spotifyUrl
        // Make sure the browser runtime is warming up (in case the clickgui was never opened yet).
        NextGenBrowserRuntime.preload(NextGenClickGuiServer.url)
        ensureBrowser()
    }

    private fun ensureBrowser() {
        if (browser != null) {
            return
        }
        val api = NextGenBrowserRuntime.readyApi() ?: return
        browser = runCatching { api.createBrowser(spotifyUrl, true) }
            .onFailure { LOGGER.error("[Spotify] Failed to create the player browser", it) }
            .getOrNull()
            ?.also { runCatching { it.resize(mc.displayWidth, mc.displayHeight) } }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val activeBrowser = browser
        if (activeBrowser != null && runCatching { activeBrowser.getTextureID() > 0 }.getOrDefault(false)) {
            val rendered = runCatching { drawBrowserTexture(activeBrowser) }.getOrElse {
                LOGGER.error("[Spotify] Player draw failed", it)
                false
            }
            if (rendered) {
                return
            }
        }
        drawLoading()
    }

    private fun drawLoading() {
        drawDefaultBackground()
        drawCenteredString(fontRendererObj, "Spotify", width / 2, height / 2 - 16, 0x1ED760)
        val status = if (NextGenBrowserRuntime.readyApi() == null) {
            NextGenBrowserRuntime.detail.ifEmpty { "Preparing the in-game browser..." }
        } else {
            "Loading player..."
        }
        drawCenteredString(fontRendererObj, status, width / 2, height / 2 + 2, 0xD0D0D0)
        drawCenteredString(fontRendererObj, "Press ESC to close", width / 2, height / 2 + 20, 0x808080)
    }

    private fun drawBrowserTexture(target: IBrowser): Boolean {
        val textureId = target.getTextureID()
        if (textureId <= 0) {
            return false
        }

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
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
        ensureBrowser()
        val target = browser ?: return
        runCatching { target.resize(mc.displayWidth, mc.displayHeight) }
        if (!focusApplied && runCatching { target.getTextureID() > 0 }.getOrDefault(false)) {
            NextGenBrowserRuntime.focus(target, true)
            focusApplied = true
        }
    }

    override fun handleInput() {
        val target = browser
        if (target == null) {
            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    mc.displayGuiScreen(null)
                    return
                }
            }
            while (Mouse.next()) { /* drain */ }
            return
        }
        dispatchKeyboard(target)
        dispatchMouse(target)
    }

    private fun dispatchKeyboard(target: IBrowser) {
        while (Keyboard.next()) {
            val keyCode = Keyboard.getEventKey()
            val keyChar = Keyboard.getEventCharacter()
            val pressed = Keyboard.getEventKeyState()

            if (pressed && keyCode == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null)
                return
            }

            val modifiers = keyboardModifiers()
            if (pressed) {
                target.injectKeyPressedByKeyCode(keyCode, keyChar, modifiers)
                if (keyChar.code != 0) {
                    target.injectKeyTyped(keyChar, modifiers)
                }
            } else {
                target.injectKeyReleasedByKeyCode(keyCode, keyChar, modifiers)
            }
        }
    }

    private fun dispatchMouse(target: IBrowser) {
        while (Mouse.next()) {
            if (mc.displayWidth == 0 || mc.displayHeight == 0) {
                continue
            }
            val x = Mouse.getEventX()
            val y = mc.displayHeight - Mouse.getEventY()
            val button = Mouse.getEventButton()
            val pressed = Mouse.getEventButtonState()
            val wheel = Mouse.getEventDWheel()

            when {
                wheel != 0 -> target.injectMouseWheel(x, y, keyboardModifiers() or pressedButtonMask, 1, wheel)
                button == -1 -> target.injectMouseMove(x, y, keyboardModifiers() or pressedButtonMask, false)
                else -> {
                    val awtButton = when (button) {
                        0 -> 1
                        1 -> 3
                        2 -> 2
                        else -> 0
                    }
                    if (awtButton == 0) {
                        continue
                    }
                    val downMask = when (awtButton) {
                        1 -> 0x400
                        2 -> 0x800
                        else -> 0x1000
                    }
                    if (pressed) {
                        if (!focusApplied || awtButton == 1) {
                            NextGenBrowserRuntime.focus(target, true)
                            focusApplied = true
                        }
                        pressedButtonMask = pressedButtonMask or downMask
                        target.injectMouseButton(x, y, keyboardModifiers() or pressedButtonMask, awtButton, true, 1)
                    } else {
                        pressedButtonMask = pressedButtonMask and downMask.inv()
                        target.injectMouseButton(x, y, keyboardModifiers() or pressedButtonMask, awtButton, false, 1)
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

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        browser?.let { runCatching { it.close() } }
        browser = null
    }

    override fun doesGuiPauseGame() = false
}
