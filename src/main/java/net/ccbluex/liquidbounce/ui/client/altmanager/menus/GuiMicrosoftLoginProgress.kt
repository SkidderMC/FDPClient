/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager.menus

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager.accountsConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.handler.lang.translationButton
import net.ccbluex.liquidbounce.handler.lang.translationText
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.login.DeviceCodeSession
import net.ccbluex.liquidbounce.utils.login.MicrosoftTitleAuth
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import kotlin.concurrent.thread

private const val CARD_HALF_WIDTH = 120f
private const val BAR_HALF_WIDTH = 110f
private const val CODE_SCALE = 2.4f
private const val SPINNER_RADIUS = 5f

private const val COLOR_TEXT = 0xffffff
private const val COLOR_MUTED = 0xa0a0a0
private const val COLOR_ERROR = 0xff5555
private const val CARD_BACKGROUND = 0x70000000

class GuiMicrosoftLoginProgress(val updateStatus: (String) -> Unit, val done: () -> Unit) : AbstractScreen() {

    @Volatile
    private var session: DeviceCodeSession? = null

    @Volatile
    private var sessionIssuedAt = 0L

    @Volatile
    private var cancelled = false

    @Volatile
    private var pendingResult: (() -> Unit)? = null

    @Volatile
    private var codeStatus = "Requesting a login code from Microsoft..."

    @Volatile
    private var failed = false

    override fun initGui() {
        startLogin()

        +GuiButton(0, width / 2 - 100, height / 2 + 60, translationButton("openURL"))
        +GuiButton(1, width / 2 - 100, height / 2 + 90, translationButton("altManager.copy"))
        +GuiButton(2, width / 2 - 100, height / 2 + 120, translationButton("cancel"))

        super.initGui()
    }

    private fun startLogin() {
        thread(name = "microsoft-device-login", isDaemon = true) {
            try {
                val deviceCode = MicrosoftTitleAuth.requestDeviceCode()
                sessionIssuedAt = System.currentTimeMillis()
                session = deviceCode

                MiscUtils.copy(deviceCode.userCode)
                codeStatus = "Waiting for you to approve the sign-in"
                updateStatus("§aLogin code ${deviceCode.userCode} copied to clipboard.")
                MiscUtils.showURL(deviceCode.verificationUri)

                val account = MicrosoftTitleAuth.awaitAccount(deviceCode) { cancelled } ?: return@thread

                if (accountsConfig.accountExists(account)) {
                    finish("§cThe account has already been added.")
                    return@thread
                }

                accountsConfig.addAccount(account)
                saveConfig(accountsConfig)
                finish("§aSuccessfully logged in.")
            } catch (e: InterruptedException) {
                LOGGER.info("Microsoft login was interrupted.")
            } catch (e: Exception) {
                LOGGER.error("Microsoft login failed.", e)
                failed = true
                codeStatus = e.message ?: "Microsoft login failed."
                finish("§c${e.message ?: "Microsoft login failed."}")
            }
        }
    }

    override fun updateScreen() {
        pendingResult?.let { result ->
            pendingResult = null
            result()
        }

        super.updateScreen()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val deviceCode = session

        assumeNonVolatile {
            drawDefaultBackground()

            Fonts.fontSemibold40.drawCenteredStringWithShadow(
                translationText("Loggingintoaccount"),
                centerX,
                centerY - 104f,
                COLOR_TEXT
            )

            drawCodeCard(centerX, centerY - 82f, deviceCode?.userCode, guiColor)

            Fonts.fontSemibold35.drawCenteredStringWithShadow(
                deviceCode?.let { "Enter it at ${it.verificationUri.removePrefix("https://")}" }
                    ?: "Contacting Microsoft...",
                centerX,
                centerY - 22f,
                COLOR_MUTED
            )

            drawTimeBar(centerX, centerY - 6f, remainingFraction(deviceCode), guiColor)
            drawStatusLine(centerX, centerY + 8f, deviceCode)
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawStatusLine(centerX: Float, y: Float, deviceCode: DeviceCodeSession?) {
        val font = Fonts.fontSemibold35
        val color = if (failed) COLOR_ERROR else COLOR_TEXT
        val textWidth = font.getStringWidth(codeStatus)

        if (!failed && deviceCode != null) {
            drawSpinner(centerX - textWidth / 2f - 12f, y + 4f)
        }

        font.drawCenteredStringWithShadow(codeStatus, centerX, y, color)

        deviceCode?.let {
            val seconds = ((it.expiresAtMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            font.drawCenteredStringWithShadow(
                "Code expires in ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                centerX,
                y + 16f,
                COLOR_MUTED
            )
        }
    }

    private fun remainingFraction(deviceCode: DeviceCodeSession?): Float {
        val total = (deviceCode?.expiresAtMillis ?: return 0f) - sessionIssuedAt

        if (total <= 0L) {
            return 0f
        }

        val left = deviceCode.expiresAtMillis - System.currentTimeMillis()

        return (left.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    override fun actionPerformed(button: GuiButton) {
        if (!button.enabled) {
            return
        }

        when (button.id) {
            0 -> session?.let { MiscUtils.showURL(it.verificationUri) }
            1 -> session?.let(::copyUserCode)
            2 -> {
                cancelled = true
                finish("§cLogin cancelled.")
            }
        }

        super.actionPerformed(button)
    }

    override fun onGuiClosed() {
        cancelled = true

        super.onGuiClosed()
    }

    private fun copyUserCode(deviceCode: DeviceCodeSession) {
        MiscUtils.copy(deviceCode.userCode)
        updateStatus("§aLogin code ${deviceCode.userCode} copied to clipboard.")
    }

    private fun finish(status: String) {
        pendingResult = {
            updateStatus(status)
            done()
        }
    }

}

private fun drawCodeCard(centerX: Float, top: Float, code: String?, accent: Int) {
    drawRoundedBorderRect(
        centerX - CARD_HALF_WIDTH,
        top,
        centerX + CARD_HALF_WIDTH,
        top + 52f,
        1.5f,
        CARD_BACKGROUND,
        accent,
        6f
    )

    GlStateManager.pushMatrix()
    GlStateManager.translate(centerX, top + 14f, 0f)
    GlStateManager.scale(CODE_SCALE, CODE_SCALE, 1f)
    Fonts.fontSemibold40.drawCenteredStringWithShadow(code ?: "- - - -", 0f, 0f, if (code == null) COLOR_MUTED else COLOR_TEXT)
    GlStateManager.popMatrix()
}

private fun drawTimeBar(centerX: Float, y: Float, fraction: Float, accent: Int) {
    drawRect(centerX - BAR_HALF_WIDTH, y, centerX + BAR_HALF_WIDTH, y + 3f, CARD_BACKGROUND)

    if (fraction > 0f) {
        drawRect(centerX - BAR_HALF_WIDTH, y, centerX - BAR_HALF_WIDTH + BAR_HALF_WIDTH * 2f * fraction, y + 3f, accent)
    }
}

private fun drawSpinner(x: Float, y: Float) {
    val rotation = (System.nanoTime() / 4_000_000L % 360L).toInt()

    drawCircle(x, y, SPINNER_RADIUS, rotation - 120, rotation)
    drawCircle(x, y, SPINNER_RADIUS, rotation + 60, rotation + 120)
}
