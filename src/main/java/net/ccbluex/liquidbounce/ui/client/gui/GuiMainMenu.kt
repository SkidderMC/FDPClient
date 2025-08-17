/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.clientVersionText
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.gui.button.ImageButton
import net.ccbluex.liquidbounce.ui.client.gui.button.QuitButton
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.ccbluex.liquidbounce.ui.font.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.utils.client.JavaVersion
import net.ccbluex.liquidbounce.utils.client.javaVersion
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils.bugs
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils.canConnect
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils.changelogs
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils.isLatest
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.io.jsonBody
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawShadowRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.lwjgl.input.Mouse

data class GithubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("published_at")
    val publishedAt: String,
    val body: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val prerelease: Boolean,
)

class GuiMainMenu : AbstractScreen(), GuiYesNoCallback {

    private var popup: PopupScreen? = null
    private var popupOnce = false

    init {
        if (!popupOnce) {
            javaVersion?.let {
                when {
                    it.major == 1 && it.minor == 8 && it.update < 100 -> showOutdatedJava8Warning()
                    it.major > 8 -> showJava11Warning()
                }
            }
            if (FileManager.firstStart) {
                showWelcomePopup()
            } else {
                checkGithubUpdate()
                checkOutdatedVersionPopup()
            }
            popupOnce = true
        }
    }

    private var logo: ResourceLocation? = null

    private lateinit var btnSinglePlayer: GuiButton
    private lateinit var btnMultiplayer: GuiButton
    private lateinit var btnClientOptions: GuiButton
    private lateinit var btnFontManager: GuiButton
    private lateinit var btnCheckUpdate: GuiButton

    private lateinit var btnClickGUI: ImageButton
    private lateinit var btnCommitInfo: ImageButton
    private lateinit var btnCosmetics: ImageButton
    private lateinit var btnMinecraftOptions: ImageButton
    private lateinit var btnLanguage: ImageButton
    private lateinit var btnForgeModList: ImageButton
    private lateinit var btnAddAccount: ImageButton

    private lateinit var btnQuit: QuitButton

    override fun initGui() {
        val basePath = "${CLIENT_NAME.lowercase()}/texture/mainmenu/"
        logo = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/mainmenu/logo.png")

        val centerY = height / 2 - 80
        val buttonWidth = 133
        val buttonHeight = 20

        btnSinglePlayer = +GuiButton(0, width / 2 - 66, centerY + 70, buttonWidth, buttonHeight, "SINGLE PLAYER")
        btnMultiplayer = +GuiButton(1, width / 2 - 66, centerY + 93, buttonWidth, buttonHeight, "MULTI PLAYER")
        btnClientOptions = +GuiButton(2, width / 2 - 66, centerY + 116, buttonWidth, buttonHeight, "SETTINGS")
        btnFontManager = +GuiButton(3, width / 2 - 66, centerY + 139, buttonWidth, buttonHeight, "FONT MANAGER")
        btnCheckUpdate = GuiButton(4, width / 2 - 66, centerY + 162, buttonWidth, buttonHeight, "§aCHECK UPDATE")

        buttonList.addAll(listOf(btnSinglePlayer, btnMultiplayer, btnClientOptions, btnFontManager, btnCheckUpdate))

        val bottomY = height - 20
        btnClickGUI = ImageButton("CLICKGUI", ResourceLocation("${basePath}clickgui.png"), width / 2 - 45, bottomY)
        btnCommitInfo = ImageButton("COMMIT INFO", ResourceLocation("${basePath}github.png"), width / 2 - 30, bottomY)
        btnCosmetics = ImageButton("COSMETICS", ResourceLocation("${basePath}cosmetics.png"), width / 2 - 15, bottomY)
        btnMinecraftOptions = ImageButton("MINECRAFT SETTINGS", ResourceLocation("${basePath}cog.png"), width / 2, bottomY)
        btnLanguage = ImageButton("LANGUAGE", ResourceLocation("${basePath}globe.png"), width / 2 + 15, bottomY)
        btnForgeModList = ImageButton("FORGE MODS", ResourceLocation("${basePath}forge.png"), width / 2 + 30, bottomY)
        btnAddAccount = ImageButton("ALT MANAGER", ResourceLocation("${basePath}add-account.png"), width - 55, 7)
        btnQuit = QuitButton(width - 17, 7)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (popup != null) {
            popup!!.mouseClicked(mouseX, mouseY, button)
            return
        }
        buttonList.forEach { guiButton ->
            if (guiButton.mousePressed(mc, mouseX, mouseY)) {
                actionPerformed(guiButton)
            }
        }
        when {
            btnQuit.hoverFade > 0 -> mc.shutdown()
            btnMinecraftOptions.hoverFade > 0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            btnLanguage.hoverFade > 0 -> mc.displayGuiScreen(GuiLanguage(this, mc.gameSettings, mc.languageManager))
            btnCommitInfo.hoverFade > 0 -> mc.displayGuiScreen(GuiCommitInfo())
            btnForgeModList.hoverFade > 0 -> mc.displayGuiScreen(GuiModList(mc.currentScreen))
            btnCosmetics.hoverFade > 0 -> mc.displayGuiScreen(GuiCommitInfo())
            btnClickGUI.hoverFade > 0 -> {
                try {
                    mc.displayGuiScreen(ClickGui)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ClickGui.initGui()
                    mc.displayGuiScreen(ClickGui)
                }
            }
            btnAddAccount.hoverFade > 0 -> mc.displayGuiScreen(GuiAltManager(this))
        }
    }

    override fun actionPerformed(button: GuiButton) {
        if (popup != null) return

        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiInfo(this))
            3 -> mc.displayGuiScreen(GuiFontManager(this))
            4 -> mc.displayGuiScreen(GuiUpdate())
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile = true
        drawBackground(0)
        if (popup != null) {
            popup?.drawScreen(width, height, mouseX, mouseY)
            assumeNonVolatile = false
            return
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            mc.displayGuiScreen(ClickGui)
        }
        GlStateManager.pushMatrix()
        drawShadowRect(
            (width / 2 - 100).toFloat(),
            (height / 2 - 80).toFloat(),
            (width / 2 + 100).toFloat(),
            (height / 2 + 112).toFloat(),
            15F,
            Color(44, 43, 43, 100).rgb
        )

        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(logo)

        drawModalRectWithCustomSizedTexture(width / 2 - 25, height / 2 - 68, 0f, 0f, 49, 49, 49f, 49f)
        val apiMessage = if (canConnect) "§eOK" else "§cNo"
        val apiTextX = width - 10f - minecraftFont.getStringWidth("API Connection: $apiMessage")
        minecraftFont.drawStringWithShadow("API Connection: $apiMessage", apiTextX, 32f, Color(255, 255, 255, 140).rgb)
        val clientNameX = width - 4f - minecraftFont.getStringWidth(CLIENT_NAME)
        minecraftFont.drawStringWithShadow(CLIENT_NAME, clientNameX, height - 23f, Color(255, 255, 255, 140).rgb)
        val uiMessage = when {
            canConnect && isLatest -> " §e(Latest)"
            !canConnect && isLatest -> " §c(API Dead)"
            else -> " §c(Outdated)"
        }
        val buildInfoText = "Your current build is $clientVersionText$uiMessage"
        val buildInfoX = width - 4f - minecraftFont.getStringWidth(buildInfoText)
        minecraftFont.drawStringWithShadow(buildInfoText, buildInfoX, height - 12f, Color(255, 255, 255, 140).rgb)

        minecraftFont.drawStringWithShadow("Changelogs:", 3f, 32f, Color(255, 255, 255, 150).rgb)

        var changeY = 48
        val changeDetails = changelogs.split("\n")
        for (line in changeDetails) {
            if (line.startsWith("* ")) continue
            val formatted = formatChangelogLine(line)
            minecraftFont.drawStringWithShadow(formatted, 4f, changeY.toFloat(), Color(255, 255, 255, 150).rgb)
            changeY += 8
        }

        val bugsFixedText = "Bugs Fixed:"
        val bugsLabelX = width - 10f - minecraftFont.getStringWidth(bugsFixedText)
        minecraftFont.drawStringWithShadow(bugsFixedText, bugsLabelX, 43f, Color(255, 255, 255, 140).rgb)

        val bugLines = bugs.split("\n").filter { !it.startsWith("#") }
        val displayBugLines = if (bugLines.size > 39) bugLines.takeLast(39) else bugLines

        var bugsY = 55

        for (line in displayBugLines) {
            val formatted = if (line.startsWith("*")) line.substring(1).trim() + " §7[§e*§7]" else line
            val lineWidth = minecraftFont.getStringWidth(formatted)
            val xPos = width - 12f - lineWidth
            minecraftFont.drawStringWithShadow(formatted, xPos, bugsY.toFloat(), Color(255, 255, 255, 140).rgb)
            bugsY += 11
        }

        Fonts.InterMedium_15.drawCenteredStringShadow("by Zywl <3 ", width / 2f, height / 2f - 25, Color(255, 255, 255, 100).rgb)

        buttonList.forEach { it.drawButton(mc, mouseX, mouseY) }

        listOf(btnClickGUI, btnCommitInfo, btnCosmetics, btnMinecraftOptions, btnLanguage, btnForgeModList, btnAddAccount, btnQuit)
            .forEach { it.drawButton(mc, mouseX, mouseY) }
        val branch = FDPClient.clientBranch
        val commitIdAbbrev = ClientUpdate.gitInfo.getProperty("git.commit.id.abbrev")
        val infoStr = "$CLIENT_NAME($branch/$commitIdAbbrev) | Minecraft 1.8.9"
        Fonts.fontSemibold35.drawCenteredString(infoStr, 7F, (height - 11).toFloat(), Color(255, 255, 255, 100).rgb)

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        GlStateManager.popMatrix()

        assumeNonVolatile = false
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    /**
     * Quick method to colorize changelog lines by prefix:
     * "~ " => "[~]"
     * "+ " => "[+]"
     * "- " => "[-]"
     */
    private fun formatChangelogLine(line: String): String {
        return when {
            line.startsWith("~ ") -> "§7[§r~§7]  §r" + line.removePrefix("~ ").trim()
            line.startsWith("+ ") -> "§7[§a+§7]  §r" + line.removePrefix("+ ").trim()
            line.startsWith("- ") -> "§7[§c-§7]  §r" + line.removePrefix("- ").trim()
            else -> line
        }
    }

    private fun showWelcomePopup() {
        popup = PopupScreen {
            title("§a§lWelcome!")
            message(
                """
                §eThank you for downloading and installing §b$CLIENT_NAME§e!
        
                §6Here is some useful information:
                §a- ClickGUI: Press §7[RightShift]§f to open ClickGUI.
                §a- Right-click modules with a '+' to edit.
                §a- Hover over a module to see its description.
        
                §6Important Commands:
                §a- .bind <module> <key> / .bind <module> none
                §a- .config load <name> / .config list
        
                §bNeed help? Contact us!
                - §fCreator: §9https://github.com/opZywl
                - §fDiscord: §9https://discord.gg/WV6qPzyqTx
                - §fGithub: §9https://github.com/SkidderMC/FDPClient
                - §fYouTube: §9https://www.youtube.com/@opZywl
                """.trimIndent()
            )
            button("§aOK")
            onClose { popup = null }
        }
    }

    private fun checkGithubUpdate() {
        screenScope.launch(Dispatchers.IO) {
            val githubRelease = fetchLatestGithubRelease()
            if (githubRelease != null && githubRelease.tagName != clientVersionText) {
                withContext(Dispatchers.Main) {
                    showUpdatePopup(githubRelease)
                }
            }
        }
    }

    private fun fetchLatestGithubRelease(): GithubRelease? = try {
        HttpClient.get("https://api.github.com/repos/SkidderMC/FDPClient/releases/latest")
            .jsonBody<GithubRelease>()
    } catch (e: Exception) {
        null
    }

    private fun showUpdatePopup(githubRelease: GithubRelease) {
        val updateType = if (!githubRelease.prerelease) "version" else "beta release"
        val dateFormatter = SimpleDateFormat("EEEE, MMMM dd, yyyy, h a z", Locale.ENGLISH)
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        inputFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val publishedDate = inputFormatter.parse(githubRelease.publishedAt)
        val formattedDate = dateFormatter.format(publishedDate)

        popup = PopupScreen {
            title("§bNew Update Available!")
            message(
                """
                §eA new $updateType of $CLIENT_NAME is available!
        
                - §aVersion:§r ${githubRelease.tagName}
                - §aPublished:§r $formattedDate
        
                §6Changes:§r
                ${githubRelease.body}
        
                §bUpgrade now to enjoy the latest features and improvements!§r
                """.trimIndent()
            )
            button("§aDownload") { MiscUtils.showURL(githubRelease.htmlUrl) }
            onClose { popup = null }
        }
    }

    private fun showOutdatedJava8Warning() {
        popup = PopupScreen {
            title("§c§lOutdated Java Runtime Environment")
            message(
                """
                §6§lYou are using an outdated version of Java 8 (${javaVersion!!.raw}).
                
                §fThis may cause unexpected §c§lBUGS§f.
                Please update to 8u101+ or download a new version from the Internet.
                """.trimIndent()
            )
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI understand")
            onClose { popup = null }
        }
    }

    private fun showJava11Warning() {
        popup = PopupScreen {
            title("§c§lInappropriate Java Runtime Environment")
            message(
                """
                §6§lThis version of $CLIENT_NAME is designed for a Java 8 environment.
                
                §fHigher versions of Java may cause bugs or crashes.
                Consider installing JRE 8.
                """.trimIndent()
            )
            button("§aDownload Java") { MiscUtils.showURL(JavaVersion.DOWNLOAD_PAGE) }
            button("§eI understand")
            onClose { popup = null }
        }
    }

    private fun checkOutdatedVersionPopup() {
        if (!isLatest && canConnect) {
            popup = PopupScreen {
                title("§bNew Update Available!")
                message(
                    """
                    §eYou are using an outdated version of $CLIENT_NAME.
                    Please update to the latest version to enjoy new features and improvements.
                    """.trimIndent()
                )
                button("§aDownload Update") { MiscUtils.showURL("https://github.com/SkidderMC/FDPClient/releases/latest") }
                onClose { popup = null }
            }
        }
    }

    override fun handleMouseInput() {
        if (popup != null) {
            val eventDWheel = Mouse.getEventDWheel()
            if (eventDWheel != 0) {
                popup!!.handleMouseWheel(eventDWheel)
            }
        }

        super.handleMouseInput()
    }
}