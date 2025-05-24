/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager.menus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.lang.translationButton
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.login.LoginUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiSessionLogin(private val prevGui: GuiAltManager) : AbstractScreen() {

    // Buttons
    private lateinit var loginButton: GuiButton

    // User Input Fields
    private lateinit var sessionTokenField: GuiTextField

    // Status
    private var status = ""

    /**
     * Initialize Session Login GUI
     */
    override fun initGui() {
        // Enable keyboard repeat events
        Keyboard.enableRepeatEvents(true)

        // Add buttons to screen

        loginButton = +GuiButton(1, width / 2 - 100, height / 2 - 60, translationButton("altManager.login"))

        +GuiButton(0, width / 2 - 100, height / 2 - 30, translationButton("back"))

        // Add fields to screen
        sessionTokenField = GuiTextField(666, Fonts.fontSemibold40, width / 2 - 100, height / 2 - 90, 200, 20)
        sessionTokenField.isFocused = false
        sessionTokenField.maxStringLength = 1000

        // Call sub method
        super.initGui()
    }

    /**
     * Draw screen
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile {
            // Draw background to screen
            drawBackground(0)
            drawRect(30f, 30f, width - 30f, height - 30f, Integer.MIN_VALUE)

            // Draw title and status
            Fonts.fontSemibold40.drawCenteredStringWithShadow("Session Login", width / 2f, height / 2 - 150f, 0xffffff)
            Fonts.fontSemibold35.drawCenteredString(status, width / 2f, height / 2f, 0xffffff)

            // Draw fields
            sessionTokenField.drawTextBox()

            if (sessionTokenField.text.isEmpty() && !sessionTokenField.isFocused)
                Fonts.fontSemibold40.drawCenteredStringWithShadow("§7Session Token", width / 2f - 60f, height / 2 - 84f, 0xffffff)
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        // Call sub method
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    /**
     * Handle button actions
     */
    override fun actionPerformed(button: GuiButton) {
        if (!button.enabled) return

        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> {
                loginButton.enabled = false
                status = "§aLogging in..."

                screenScope.launch(Dispatchers.IO) {
                    val loginResult = LoginUtils.loginSessionId(sessionTokenField.text)

                    status = when (loginResult) {
                        LoginUtils.LoginResult.LOGGED -> {
                            "§aLogged into §f§l${mc.session.username}§a."
                        }
                        LoginUtils.LoginResult.FAILED_PARSE_TOKEN -> "§cFailed to parse Session ID!"
                        LoginUtils.LoginResult.INVALID_ACCOUNT_DATA -> "§cInvalid Session ID!"
                    }

                    loginButton.enabled = true
                }
            }
        }
    }

    /**
     * Handle key typed
     */
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            // Check if user want to escape from screen
            Keyboard.KEY_ESCAPE -> {
                // Send back to prev screen
                mc.displayGuiScreen(prevGui)
                return
            }

            Keyboard.KEY_TAB -> {
                sessionTokenField.isFocused = true
                return
            }

            Keyboard.KEY_RETURN -> {
                actionPerformed(loginButton)
                return
            }
        }

        // Check if field is focused, then call key typed
        if (sessionTokenField.isFocused) sessionTokenField.textboxKeyTyped(typedChar, keyCode)

        // Call sub method
        super.keyTyped(typedChar, keyCode)
    }

    /**
     * Handle mouse clicked
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Call mouse clicked to field
        sessionTokenField.mouseClicked(mouseX, mouseY, mouseButton)

        // Call sub method
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Handle screen update
     */
    override fun updateScreen() {
        sessionTokenField.updateCursorCounter()
        super.updateScreen()
    }

    /**
     * Handle gui closed
     */
    override fun onGuiClosed() {
        // Disable keyboard repeat events
        Keyboard.enableRepeatEvents(false)

        // Call sub method
        super.onGuiClosed()
    }
}
