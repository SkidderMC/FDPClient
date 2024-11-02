/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient.IN_DEV
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.APIConnecter.checkBugs
import net.ccbluex.liquidbounce.utils.APIConnecter.checkChangelogs
import net.ccbluex.liquidbounce.utils.APIConnecter.checkStatus
import net.ccbluex.liquidbounce.utils.APIConnecter.loadDonors
import net.ccbluex.liquidbounce.utils.APIConnecter.loadPictures
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class GuiUpdate : GuiScreen() {

    private var isLoading = false
    private var loadProgress = 0
    private var errorMessage: String? = null  // Store any error message

    override fun initGui() {
        val j = height / 4 + 24

        buttonList.run {
            add(GuiButton(1, width / 2 + 2, j + 24 * 2, 98, 20, "Ignore"))
            add(GuiButton(2, width / 2 - 100, j + 24 * 2, 98, 20, "Go to Update page"))
            add(GuiButton(3, width / 2 - 49, j + 24 * 3, 98, 20, "Reload API"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val messageYPosition = (height / 8f + 60).toInt()
        val titleYPosition = (height / 16f + 10).toInt()

        val mainMessage = if (!IN_DEV) {
            "${ClientUpdate.newestVersion?.lbVersion} got released!"
        } else {
            "New build available!"
        }
        val mainMessageWidth = Fonts.minecraftFont.getStringWidth(mainMessage)

        Fonts.minecraftFont.drawStringWithShadow(
            mainMessage,
            (width / 2f - mainMessageWidth / 2),
            messageYPosition.toFloat(),
            0xffffff
        )

        val subMessage = "Press \"Download\" to visit our website or dismiss this message by pressing \"OK\"."
        val subMessageWidth = Fonts.minecraftFont.getStringWidth(subMessage)

        Fonts.minecraftFont.drawStringWithShadow(
            subMessage,
            (width / 2f - subMessageWidth / 2),
            (messageYPosition + Fonts.minecraftFont.FONT_HEIGHT).toFloat(),
            0xffffff
        )

        glPushMatrix()
        glScalef(2F, 2F, 2F)
        val titleWidth = Fonts.minecraftFont.getStringWidth("New update available!") / 2
        Fonts.minecraftFont.drawStringWithShadow(
            "New update available!",
            (width / 4f) - titleWidth,
            titleYPosition / 2f,
            Color(255, 0, 0).rgb
        )
        glPopMatrix()

        if (isLoading) {
            drawLoadingBar()
        }

        errorMessage?.let {
            val errorWidth = Fonts.minecraftFont.getStringWidth(it)
            Fonts.minecraftFont.drawStringWithShadow(it, (width / 2f - errorWidth / 2), height - 30f, Color.RED.rgb)
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawLoadingBar() {
        val barWidth = width / 2
        val barX = (width - barWidth) / 2
        val barY = height - 50
        val filledWidth = (barWidth * (loadProgress / 100.0)).toInt()

        drawRect(barX, barY, barX + barWidth, barY + 10, Color(50, 50, 50, 150).rgb)
        drawRect(barX, barY, barX + filledWidth, barY + 10, Color(0, 200, 0).rgb)

        val progressText = "$loadProgress%"
        val textWidth = Fonts.minecraftFont.getStringWidth(progressText)
        Fonts.minecraftFont.drawStringWithShadow(
            progressText,
            (width / 2f - textWidth / 2),
            (barY - Fonts.minecraftFont.FONT_HEIGHT).toFloat(),
            0xffffff
        )

        if (isLoading) {
            loadProgress += 10
            if (loadProgress >= 100) {
                loadProgress = 100
                isLoading = false
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiMainMenu())
            2 -> MiscUtils.showURL("https://fdpinfo.github.io/download")
            3 -> {
                isLoading = true
                loadProgress = 0
                errorMessage = null

                    try {
                        checkStatus()
                        checkChangelogs()
                        checkBugs()
                        loadPictures()
                        loadDonors()
                    } catch (e: Exception) {
                        errorMessage = "Failed to reload API: ${e.message}"
                    } finally {
                        isLoading = false
                        loadProgress = 100
                    }
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null)
        } else {
            super.keyTyped(typedChar, keyCode)
        }
    }
}
