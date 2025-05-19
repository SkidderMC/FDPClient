/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.IOException
import kotlin.Float
import kotlin.Int
import kotlin.Throws

class GuiCommitInfo : AbstractScreen() {

    private val gitImage: ResourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/mainmenu/github.png")

    override fun initGui() {
        val buttonWidth = 200
        val buttonHeight = 20
        val buttonX = width / 2 - buttonWidth / 2
        val buttonY = height - buttonHeight - 10

        +GuiButton(0, buttonX, buttonY, buttonWidth, buttonHeight, "Back")

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        assumeNonVolatile = true

        drawDefaultBackground()
        drawImage(gitImage, 30, 30, 30, 30)

        val font = Fonts.minecraftFont
        val textColor = Color(255, 255, 255).rgb
        val startX = 70
        val startY = 30

        val lines = listOf(
            "Git Info",
            "$CLIENT_NAME built by ${ClientUpdate.gitInfo.getProperty("git.build.user.name")}",
            "Version: ${ClientUpdate.gitInfo.getProperty("git.build.version")}",
            "CommitId: ${ClientUpdate.gitInfo.getProperty("git.commit.id")} (${ClientUpdate.gitInfo.getProperty("git.commit.id.abbrev")})",
            "CommitMessage: ${ClientUpdate.gitInfo.getProperty("git.commit.message.short")}",
            "Branch: ${ClientUpdate.gitInfo.getProperty("git.branch")}",
            "Remote origin: ${ClientUpdate.gitInfo.getProperty("git.remote.origin.url")}",
            "Developers: ${FDPClient.CLIENT_AUTHOR}"
        )

        lines.forEachIndexed { index, line ->
            drawString(font, line, startX, startY + font.FONT_HEIGHT * index + index * 5, textColor)
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        assumeNonVolatile = false

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(null)
        }
    }
}