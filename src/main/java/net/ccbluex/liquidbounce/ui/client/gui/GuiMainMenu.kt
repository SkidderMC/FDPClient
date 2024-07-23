/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.clientVersionText
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.client.gui.button.ImageButton
import net.ccbluex.liquidbounce.ui.client.gui.button.QuitButton
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.ccbluex.liquidbounce.utils.APIConnecter.bugs
import net.ccbluex.liquidbounce.utils.APIConnecter.canConnect
import net.ccbluex.liquidbounce.utils.APIConnecter.changelogs
import net.ccbluex.liquidbounce.utils.APIConnecter.checkBugs
import net.ccbluex.liquidbounce.utils.APIConnecter.checkChangelogs
import net.ccbluex.liquidbounce.utils.APIConnecter.checkStatus
import net.ccbluex.liquidbounce.utils.APIConnecter.isLatest
import net.ccbluex.liquidbounce.utils.APIConnecter.loadDonors
import net.ccbluex.liquidbounce.utils.APIConnecter.loadPictures
import net.ccbluex.liquidbounce.utils.GitUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawShadowRect
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color
import java.util.*

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private var logo: ResourceLocation? = null
    private lateinit var btnSinglePlayer: GuiButton
    private lateinit var btnMultiplayer: GuiButton
    private lateinit var btnClientOptions: GuiButton
    private lateinit var btnConnectAPI: ImageButton
    private lateinit var btnCommitInfo: ImageButton
    private lateinit var btnCosmetics: ImageButton
    private lateinit var btnMinecraftOptions: ImageButton
    private lateinit var btnLanguage: ImageButton
    private lateinit var btnForgeModList: ImageButton
    private lateinit var btnQuit: QuitButton
    private lateinit var btnAddAccount: ImageButton

    private val guiCapeManager: GuiCapeManager = GuiCapeManager

    override fun initGui() {
        logo = ResourceLocation("fdpclient/mainmenu/logo.png")
        val yPos = height - 20
        val buttonWidth = 133
        val buttonHeight = 20

        btnSinglePlayer = GuiButton(0, width / 2 - 66, height / 2 - 80 + 70, buttonWidth, buttonHeight, "SINGLE PLAYER")
        btnMultiplayer = GuiButton(1, width / 2 - 66, height / 2 - 80 + 95 - 2, buttonWidth, buttonHeight, "MULTI PLAYER")
        btnClientOptions = GuiButton(2, width / 2 - 66, height / 2 - 80 + 120 - 4, buttonWidth, buttonHeight, "SETTINGS")

        btnCommitInfo = ImageButton(
            "COMMIT INFO",
            ResourceLocation("fdpclient/mainmenu/github.png"),
            width / 2 - 30,
            yPos
        )
        btnCosmetics = ImageButton(
            "COSMETICS",
            ResourceLocation("fdpclient/mainmenu/cosmetics.png"),
            width / 2 - 15,
            yPos
        )
        btnMinecraftOptions = ImageButton(
            "MINECRAFT SETTINGS",
            ResourceLocation("fdpclient/mainmenu/cog.png"),
            width / 2,
            yPos
        )
        btnLanguage = ImageButton(
            "LANGUAGE",
            ResourceLocation("fdpclient/mainmenu/globe.png"),
            width / 2 + 15,
            yPos
        )
        btnForgeModList = ImageButton(
            "FORGE MODS",
            ResourceLocation("fdpclient/mainmenu/forge.png"),
            width / 2 + 30,
            yPos
        )
        btnAddAccount = ImageButton(
            "ALT MANAGER",
            ResourceLocation("fdpclient/mainmenu/add-account.png"),
            width - 55,
            7
        )
        btnConnectAPI = ImageButton(
            "Connect API",
            ResourceLocation("fdpclient/mainmenu/reload.png"),
            width - 37,
            7
        )
        btnQuit = QuitButton(width - 17, 7)

        buttonList.addAll(listOf(btnSinglePlayer, btnMultiplayer, btnClientOptions))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        buttonList.forEach { guiButton ->
            if (guiButton.mousePressed(mc, mouseX, mouseY)) {
                actionPerformed(guiButton)
            }

            when {
                btnQuit.hoverFade > 0 -> mc.shutdown()
                btnMinecraftOptions.hoverFade > 0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
                btnLanguage.hoverFade > 0 -> mc.displayGuiScreen(GuiLanguage(this, mc.gameSettings, mc.languageManager))
                btnCommitInfo.hoverFade > 0 -> mc.displayGuiScreen(GuiCommitInfo())
                btnForgeModList.hoverFade > 0 -> mc.displayGuiScreen(GuiModList(mc.currentScreen))
                btnCosmetics.hoverFade > 0 -> mc.displayGuiScreen(guiCapeManager)
                btnAddAccount.hoverFade > 0 -> mc.displayGuiScreen(GuiAltManager(this))
                btnConnectAPI.hoverFade > 0 -> {
                    checkStatus()
                    checkChangelogs()
                    checkBugs()
                    loadPictures()
                    loadDonors()
                }
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiInfo(this))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        GlStateManager.pushMatrix()
        drawShadowRect(
            (width / 2 - 130).toFloat(),
            (height / 2 - 90).toFloat(),
            (width / 2 + 130).toFloat(),
            (height / 2 + 90).toFloat(),
            15F,
            Color(44, 43, 43, 100).rgb.toFloat().toInt()
        )

        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(logo)
        drawModalRectWithCustomSizedTexture(width / 2 - 25, height / 2 - 68, 0f, 0f, 49, 49, 49f, 49f)

        val apiMessage = if (canConnect) "§eOK" else "§cNo"
        minecraftFont.drawStringWithShadow(
            "API Connection: $apiMessage",
            ((width - 10f - minecraftFont.getStringWidth("API Connection: $apiMessage")).toDouble().toFloat()),
            32.0F,
            Color(255, 255, 255, 100).rgb
        )
        minecraftFont.drawStringWithShadow(
            CLIENT_NAME,
            ((width - 4f - minecraftFont.getStringWidth(CLIENT_NAME)).toDouble().toFloat()),
            ((height - 23f).toDouble().toFloat()),
            Color(255, 255, 255, 100).rgb
        )
        val uiMessage =
            if (canConnect && isLatest) " §e(Latest)" else if (!canConnect && isLatest) " §c(API Dead)" else " §c(Outdated)"
        minecraftFont.drawStringWithShadow(
            "Your currently build is $clientVersionText$uiMessage",
            ((width - 4f - minecraftFont.getStringWidth("Your currently build is $clientVersionText$uiMessage")).toDouble().toFloat()),
            ((height - 12f).toDouble().toFloat()),
            Color(255, 255, 255, 100).rgb
        )
        minecraftFont.drawStringWithShadow(
            "Changelogs:",
            3.0F,
            32.0F,
            Color(255, 255, 255, 100).rgb
        )
        var changeY = 48
        val changeDetails: List<String> = changelogs.split("\n")
        changeDetails.forEach { detail ->
            val formattedDetail = when {
                detail.startsWith("~ ") -> "§r " + detail.uppercase(Locale.getDefault())
                detail.startsWith("+ ") -> "§7[§a+§7]  §r${detail.replace("+ ", "").trim()}"
                detail.startsWith("- ") -> "§7[§c-§7]  §r${detail.replace("- ", "").trim()}"
                detail.startsWith("* ") -> "§7[§e*§7]  §r${detail.replace("* ", "").trim()}"
                else -> detail
            }
            minecraftFont.drawStringWithShadow(
                formattedDetail,
                4.0F,
                changeY.toFloat().toDouble().toFloat(),
                Color(255, 255, 255, 100).rgb
            )
            changeY += 8
        }
        minecraftFont.drawStringWithShadow(
            "Known Bugs:",
            ((width - 10f - minecraftFont.getStringWidth("Known Bugs:")).toDouble().toFloat()),
            43.0F,
            Color(255, 255, 255, 100).rgb
        )
        var bugsY = 55
        val bugDetails: List<String> = bugs.split("\n")
        bugDetails.forEach { detail ->
            minecraftFont.drawStringWithShadow(
                detail,
                ((width - 12f - minecraftFont.getStringWidth(detail)).toDouble().toFloat()),
                bugsY.toFloat().toDouble().toFloat(),
                Color(255, 255, 255, 100).rgb
            )
            bugsY += 11
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
        Fonts.fontSmall.drawCenteredStringWithoutShadow(
            "by SkidderMC with love ",
            width.toFloat() / 2, height.toFloat() / 2 - 19, Color(255, 255, 255, 100).rgb
        )

        listOf(btnSinglePlayer, btnMultiplayer, btnClientOptions).forEach {
            it.drawButton(mc, mouseX, mouseY)
        }
        listOf(btnConnectAPI, btnCommitInfo, btnCosmetics, btnMinecraftOptions, btnLanguage, btnForgeModList, btnAddAccount, btnQuit).forEach {
            it.drawButton(mouseX, mouseY)
        }

        Fonts.font35.drawString(
            ((CLIENT_NAME + "(" + GitUtils.gitBranch) + "/" + GitUtils.gitInfo.getProperty(
                "git.commit.id.abbrev"
            )) + ") | Minecraft 1.8.9", 7, (this.height - 11).toFloat().toInt(), Color(255, 255, 255, 100).rgb
        )

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        GlStateManager.popMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}