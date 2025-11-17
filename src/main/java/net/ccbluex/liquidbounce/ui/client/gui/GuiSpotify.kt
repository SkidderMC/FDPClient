/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule.SpotifyAuthMode
import net.ccbluex.liquidbounce.handler.spotify.SpotifyIntegration
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraftforge.fml.client.config.GuiSlider
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class GuiSpotify(private val previousScreen: GuiScreen?) : AbstractScreen() {

    private lateinit var clientIdField: GuiTextField
    private lateinit var clientSecretField: GuiTextField
    private lateinit var refreshTokenField: GuiTextField
    private lateinit var reconnectButton: GuiButton
    private lateinit var modeButton: GuiButton
    private lateinit var saveButton: GuiButton
    private lateinit var pollSlider: GuiSlider
    private val fieldDecorations = mutableMapOf<GuiTextField, FieldDecoration>()
    private val fieldEnabledStates = mutableMapOf<GuiTextField, Boolean>()
    private var browserAuthStatus: Pair<SpotifyModule.BrowserAuthStatus, String>? = null

    private val inputBackgroundColor = Color(9, 9, 9, 185).rgb
    private val inputBorderColor = Color(255, 255, 255, 60).rgb
    private val labelColor = 0xFFE3E3E3.toInt()
    private val helperColor = 0xFFB6B6B6.toInt()

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        buttonList.clear()
        textFields.clear()
        fieldDecorations.clear()
        fieldEnabledStates.clear()

        val fieldWidth = 260
        val startX = width / 2 - fieldWidth / 2
        var currentY = height / 4

        clientIdField = textField(0, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 128
            text = SpotifyModule.clientId
            enableBackgroundDrawing = false
        }
        registerField(clientIdField)
        fieldDecorations[clientIdField] = FieldDecoration("Client ID", "Use the Spotify app identifier from your dashboard.")
        currentY += 44

        clientSecretField = textField(1, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 128
            text = SpotifyModule.clientSecret
            enableBackgroundDrawing = false
        }
        registerField(clientSecretField)
        fieldDecorations[clientSecretField] = FieldDecoration("Client secret", "Generated with the same app as the client ID.")
        currentY += 44

        refreshTokenField = textField(2, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 256
            text = SpotifyModule.refreshToken
            enableBackgroundDrawing = false
        }
        registerField(refreshTokenField)
        fieldDecorations[refreshTokenField] = FieldDecoration("Refresh token", "Paste the long-lived token from your Spotify app setup.")
        currentY += 44

        pollSlider = GuiSlider(
            3,
            startX,
            currentY,
            fieldWidth,
            20,
            "Poll interval (",
            "s)",
            3.0,
            60.0,
            SpotifyModule.pollIntervalSeconds.toDouble(),
            false,
            true,
        ) { slider ->
            SpotifyModule.setPollInterval(slider.valueInt)
        }
        +pollSlider
        currentY += 26

        modeButton = GuiButton(10, startX, currentY, fieldWidth, 20, SpotifyModule.authModeLabel())
        +modeButton
        currentY += 24

        reconnectButton = +GuiButton(4, startX, currentY, fieldWidth, 20, reconnectLabel())
        currentY += 24

        saveButton = GuiButton(5, startX, currentY, fieldWidth, 20, "Save credentials")
        +saveButton
        currentY += 24

        +GuiButton(6, startX, currentY, fieldWidth, 20, "Authorize via Browser")
        currentY += 24

        +GuiButton(7, startX, currentY, fieldWidth, 20, "Open Spotify Dashboard")
        currentY += 24

        +GuiButton(8, startX, currentY, fieldWidth, 20, "Authorization Guide")
        currentY += 24

        +GuiButton(9, startX, currentY, fieldWidth, 20, "Back")

        refreshAuthModeUi()
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            4 -> {
                SpotifyModule.toggleAutoReconnect()
                button.displayString = reconnectLabel()
            }

            5 -> {
                val saved = SpotifyModule.updateCredentials(
                    clientIdField.text.trim(),
                    clientSecretField.text.trim(),
                    refreshTokenField.text.trim(),
                )
                if (saved) {
                    chat("§aSaved Spotify credentials to ${SpotifyModule.credentialsFilePath()}.")
                } else {
                    chat("§cFailed to save Spotify credentials. Check the log for details.")
                }
            }
            6 -> {
                SpotifyModule.beginBrowserAuthorization { status, message ->
                    browserAuthStatus = status to message
                    if (status == SpotifyModule.BrowserAuthStatus.SUCCESS && SpotifyModule.authMode == SpotifyAuthMode.MANUAL) {
                        refreshTokenField.text = SpotifyModule.refreshToken
                    }
                    val prefix = when (status) {
                        SpotifyModule.BrowserAuthStatus.INFO -> "§e"
                        SpotifyModule.BrowserAuthStatus.SUCCESS -> "§a"
                        SpotifyModule.BrowserAuthStatus.ERROR -> "§c"
                    }
                    chat(prefix + message)
                }
            }

            7 -> SpotifyIntegration.openDashboard()
            8 -> SpotifyIntegration.openGuide()
            9 -> mc.displayGuiScreen(previousScreen)
            10 -> {
                val previousMode = SpotifyModule.authMode
                val newMode = SpotifyModule.cycleAuthMode()
                if (newMode != previousMode) {
                    chat("§eSwitched Spotify mode to ${newMode.displayName}.")
                }
                refreshAuthModeUi()
            }
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        Keyboard.enableRepeatEvents(false)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val titleFont = Fonts.fontRegular40
        val smallFont = Fonts.fontSemibold35
        titleFont.drawCenteredString("Spotify Integration", width / 2f, height / 4f - 40f, -1, true)

        val connectionText = "State: ${SpotifyModule.connectionState.displayName}"
        smallFont.drawCenteredString(connectionText, width / 2f, height / 4f - 20f, -1, true)
        drawModeInfo(smallFont)

        val currentState = SpotifyModule.currentState
        drawPlaybackInfo(currentState)

        val error = SpotifyModule.lastErrorMessage
        if (!error.isNullOrBlank()) {
            smallFont.drawCenteredString("Last error: $error", width / 2f, height - 48f, 0xFF5555, true)
        }

        val configPathText = "Config file: ${SpotifyModule.credentialsFilePath()}"
        smallFont.drawCenteredString(configPathText, width / 2f, height - 32f, 0xFFB0B0B0.toInt(), true)
        browserAuthStatus?.let { (status, message) ->
            val color = when (status) {
                SpotifyModule.BrowserAuthStatus.INFO -> 0xFFE0B45A.toInt()
                SpotifyModule.BrowserAuthStatus.SUCCESS -> 0xFF6DE37B.toInt()
                SpotifyModule.BrowserAuthStatus.ERROR -> 0xFFE05757.toInt()
            }
            smallFont.drawCenteredString("Browser auth: $message", width / 2f, height - 16f, color, true)
        }

        drawInputField(clientIdField)
        drawInputField(clientSecretField)
        drawInputField(refreshTokenField)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen)
            return
        }

        if ((isFieldEnabled(clientIdField) && clientIdField.textboxKeyTyped(typedChar, keyCode)) ||
            (isFieldEnabled(clientSecretField) && clientSecretField.textboxKeyTyped(typedChar, keyCode)) ||
            (isFieldEnabled(refreshTokenField) && refreshTokenField.textboxKeyTyped(typedChar, keyCode))
        ) {
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    private fun drawPlaybackInfo(state: SpotifyState?) {
        val infoFont = Fonts.fontSemibold35
        val lines = mutableListOf<String>()
        val trackState = state
        if (trackState != null && trackState.track != null) {
            val track = trackState.track
            lines += "Track: ${track.title}"
            lines += "Artists: ${track.artists}"
            if (track.album.isNotBlank()) {
                lines += "Album: ${track.album}"
            }
            val progressText = if (track.durationMs > 0) {
                val total = formatMillis(track.durationMs)
                "Progress: ${formatMillis(trackState.progressMs)} / $total"
            } else {
                "Progress: ${formatMillis(trackState.progressMs)}"
            }
            lines += progressText
            lines += if (trackState.isPlaying) "Status: Playing" else "Status: Paused"
        } else {
            lines += "No playback detected."
            lines += "Start Spotify on any device to see the track information."
        }

        val startY = height / 2 + 30
        lines.forEachIndexed { index, line ->
            infoFont.drawCenteredString(line, width / 2f, (startY + index * 12).toFloat(), -1, true)
        }
    }

    private fun drawInputField(field: GuiTextField) {
        val info = fieldDecorations[field] ?: return
        val labelFont = Fonts.fontSemibold35
        val helperFont = Fonts.fontSemibold35
        val padding = 4f
        val x = field.xPosition - padding
        val y = field.yPosition - padding
        val width = field.width + padding * 2
        val height = field.height + padding * 2
        val enabled = isFieldEnabled(field)
        val background = if (enabled) inputBackgroundColor else Color(20, 20, 20, 120).rgb
        val border = if (enabled) inputBorderColor else Color(255, 255, 255, 50).rgb
        val labelTint = if (enabled) labelColor else helperColor
        val helperTint = if (enabled) helperColor else helperColor

        RenderUtils.drawRoundedRect(
            x,
            y,
            width,
            height,
            4f,
            background,
            1.2f,
            border,
        )

        labelFont.drawString(info.label, field.xPosition.toFloat(), (field.yPosition - 12).toFloat(), labelTint)
        helperFont.drawString(info.helper, field.xPosition.toFloat(), (field.yPosition + field.height + 6).toFloat(), helperTint)

        field.drawTextBox()
    }

    private fun drawModeInfo(font: GameFontRenderer) {
        val modeText = SpotifyModule.authModeLabel()
        font.drawCenteredString(modeText, width / 2f, height / 4f - 4f, -1, true)
        val helperText = if (SpotifyModule.authMode == SpotifyAuthMode.QUICK) {
            "Quick connect uses FDP's built-in Spotify app. Just authorize via browser."
        } else {
            "Manual mode uses your own Spotify app credentials."
        }
        font.drawCenteredString(helperText, width / 2f, height / 4f + 10f, helperColor, true)
    }

    private fun formatMillis(position: Int): String {
        val safePosition = max(0, position)
        val minutes = safePosition / 1000 / 60
        val seconds = (safePosition / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun reconnectLabel(): String = "Auto reconnect: ${if (SpotifyModule.autoReconnect) "On" else "Off"}"

    private fun refreshAuthModeUi() {
        val manualMode = SpotifyModule.authMode == SpotifyAuthMode.MANUAL
        setFieldEnabled(clientIdField, manualMode)
        setFieldEnabled(clientSecretField, manualMode)
        setFieldEnabled(refreshTokenField, manualMode)
        saveButton.enabled = manualMode
        modeButton.displayString = SpotifyModule.authModeLabel()
        modeButton.enabled = SpotifyModule.supportsQuickConnect()
        if (manualMode) {
            clientIdField.text = SpotifyModule.clientId
            clientSecretField.text = SpotifyModule.clientSecret
            refreshTokenField.text = SpotifyModule.refreshToken
        }
    }

    private fun registerField(field: GuiTextField) {
        fieldEnabledStates[field] = true
    }

    private fun setFieldEnabled(field: GuiTextField, enabled: Boolean) {
        fieldEnabledStates[field] = enabled
        field.setEnabled(enabled)
    }

    private fun isFieldEnabled(field: GuiTextField): Boolean = fieldEnabledStates[field] ?: true

    private data class FieldDecoration(val label: String, val helper: String)
}