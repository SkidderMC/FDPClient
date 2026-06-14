/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import com.mojang.realmsclient.gui.ChatFormatting
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_GITHUB
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.SideGui
import net.ccbluex.liquidbounce.utils.render.StencilUtils
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.Configs
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.NeverloseConfigManager
import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import net.ccbluex.liquidbounce.utils.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.BloomUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.GaussianBlur
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.client.gui.GuiUpdate
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.ccbluex.liquidbounce.ui.font.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindManager
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import net.ccbluex.liquidbounce.utils.io.MiscUtils

class NeverloseGui : GuiScreen() {

    private val sideGui = SideGui()

    private var viewerOpen = true
    private var espPreviewComponent = EspPreviewComponent(this)

    var x = 100
    var y = 100
    var w = 500
    var h = 380
    var alphaani: Animation? = null
    var selectedSub: NlSub? = null
    val nlTabs: MutableList<NlTab> = ArrayList()
    var loader = true
    private var x2 = 0
    private var y2 = 0
    private var dragging = false
    private var settings = false
    private var search = false
    private var searchText = ""
    private var searchAllCategories = true

    // Search-mode header widgets (populated each frame while the search panel is visible)
    private var searchCloseRect = floatArrayOf(0f, 0f, 0f, 0f)
    private var searchAllChipRect = floatArrayOf(0f, 0f, 0f, 0f)
    private var searchCatChipRect = floatArrayOf(0f, 0f, 0f, 0f)
    private var searchPanelRect = floatArrayOf(0f, 0f, 0f, 0f)

    private val clientPath = CLIENT_NAME.lowercase(Locale.getDefault())
    private val defaultAvatar = ResourceLocation("$clientPath/64.png")

    private val headerIconHitboxes = mutableListOf<HeaderIconHitbox>()
    private var avatarTexture: ResourceLocation = defaultAvatar
    private var avatarLoaded = false
    private var nlSetting: NlSetting = NlSetting()
    private val searchanim: Animation = EaseInOutQuad(400, 1.0, Direction.BACKWARDS)
    val configs = Configs()
    private val configManager = NeverloseConfigManager()
    private var bloomFramebuffer = Framebuffer(1, 1, false)
    private var previousDebugInfoState = false

    init {
        INSTANCE = this
        var y2 = 0
        var u2 = 0
        val orderedCategories: MutableList<Category> = ArrayList()
        orderedCategories.add(Category.CLIENT)
        for (type in Category.entries) {
            if (!orderedCategories.contains(type)) {
                orderedCategories.add(type)
            }
        }
        for (type in orderedCategories) {
            if (type.name.equals("World", true) || type.name.equals("Interface", true)) continue
            nlTabs.add(NlTab(type, u2 + y2 + 40))
            for (subCategory in type.subCategories) {
                u2 += 17
            }
            y2 += 14
        }
    }

    override fun initGui() {
        super.initGui()
        configManager.refresh()
        previousDebugInfoState = mc.gameSettings.showDebugInfo
        mc.gameSettings.showDebugInfo = false
        alphaani = EaseInOutQuad(300, 0.6, Direction.FORWARDS)
        sideGui.initGui()
    }

    override fun onGuiClosed() {
        mc.gameSettings.showDebugInfo = previousDebugInfoState
        super.onGuiClosed()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GL11.glPushMatrix()
        if (loader && nlTabs.isNotEmpty()) {
            selectedSub = nlTabs[0].nlSubList[0]
            loader = false
        }
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }
        bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer)
        bloomFramebuffer.framebufferClear()
        bloomFramebuffer.bindFramebuffer(true)
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 4f, if (light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        bloomFramebuffer.unbindFramebuffer()
        BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, 6, 3)
        StencilUtils.initStencilToWrite()
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 4f, if (light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        StencilUtils.readStencilBuffer(1)
        GaussianBlur.renderBlur(10F)
        StencilUtils.uninitStencilBuffer()
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 2f, if (light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        RoundedUtil.drawRound((x + 90).toFloat(), (y + HEADER_HEIGHT).toFloat(), (w - 90).toFloat(), (h - HEADER_HEIGHT).toFloat(), 1f, if (light) Color(255, 255, 255) else Color(9, 9, 9))
        RoundedUtil.drawRound((x + 90).toFloat(), y.toFloat(), (w - 90).toFloat(), HEADER_HEIGHT.toFloat(), 1f, if (light) Color(255, 255, 255) else Color(9, 9, 9))
        RoundedUtil.drawRound((x + 90).toFloat(), (y + HEADER_HEIGHT - 1).toFloat(), (w - 90).toFloat(), 1f, 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))
        RoundedUtil.drawRound((x + 89).toFloat(), y.toFloat(), 1f, h.toFloat(), 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))
        GL11.glEnable(GL11.GL_BLEND)
        ensureAvatarTexture()
        mc.textureManager.bindTexture(avatarTexture)
        val footerLineY = y + h - 35
        val avatarY = footerLineY + 9
        RoundedUtil.drawRoundTextured((x + 4).toFloat(), avatarY.toFloat(), 20f, 20f, 10f, 1f)
        Fonts.Nl_18.drawString(mc.session.username, (x + 29).toFloat(), (avatarY + 1).toFloat(), if (light) Color(51, 51, 51).rgb else -1)
        Fonts.Nl_16.drawString(ChatFormatting.GRAY.toString() + "Till: " + ChatFormatting.RESET + SimpleDateFormat("dd:MM").format(Date()) + " " + SimpleDateFormat("HH:mm").format(Date()), (x + 29).toFloat(), (avatarY + 13).toFloat(), neverlosecolor.rgb)

        val fdpString = "FDP"
        val fdpWidth = Fonts.NLBold_28.stringWidth(fdpString)
        val centerX = x + (90 - fdpWidth) / 2f

        if (!light) {
            NLOutline(fdpString, Fonts.NLBold_28, centerX, (y + 12).toFloat(), -1, neverlosecolor.rgb, 0.7f)
        } else {
            Fonts.NLBold_28.drawString(fdpString, centerX, (y + 12).toFloat(), Color(51, 51, 51).rgb, false)
        }

        RoundedUtil.drawRound(x.toFloat(), footerLineY.toFloat(), 89f, 1f, 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))

        val bgMouseX = if (sideGui.focused) -1 else mouseX
        val bgMouseY = if (sideGui.focused) -1 else mouseY

        for (nlTab in nlTabs) {
            nlTab.x = x
            nlTab.y = y
            nlTab.w = w
            nlTab.h = h
            nlTab.draw(bgMouseX, bgMouseY)
        }

        searchanim.direction = if (search) Direction.FORWARDS else Direction.BACKWARDS
        val searchProgress = searchanim.output.toFloat()

        // Top-right toggles: settings gear ("x") and search ("j")
        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("x", (x + w - 50).toFloat(), (y + 17).toFloat(), if (settings) neverlosecolor.rgb else if (light) Color(95, 95, 95).rgb else -1)
        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("j", (x + w - 30).toFloat(), (y + 18).toFloat(), if (search) neverlosecolor.rgb else if (light) Color(95, 95, 95).rgb else -1)

        if (settings) {
            nlSetting.draw(mouseX, mouseY)
        }

        val headerIcons = listOf(
            HeaderIcon("GitHub", "G") { MiscUtils.showURL(CLIENT_GITHUB) },
            HeaderIcon("Edit", "E") { mc.displayGuiScreen(GuiHudDesigner()) },
            HeaderIcon("Viewer", "V") {
                viewerOpen = !viewerOpen
                if (viewerOpen) {
                    espPreviewComponent = EspPreviewComponent(this)
                }
            },
            HeaderIcon("Spotify", "S") { SpotifyModule.openPlayerScreen() },
            HeaderIcon("Keybind", "K") { mc.displayGuiScreen(KeyBindManager) },

            HeaderIcon("Support", "H") { MiscUtils.showURL("https://github.com/opZywl/fdpclient/issues") },
            HeaderIcon("Update", "U") { mc.displayGuiScreen(GuiUpdate()) },
            HeaderIcon("Theme", "T") { sideGui.openCategory("Color") },
            HeaderIcon("Discord", "D") { MiscUtils.showURL("https://discord.com/invite/3XRFGeqEYD") },
            HeaderIcon("Fonts", "F") { mc.displayGuiScreen(GuiFontManager(this)) }
        )

        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()

        // ----- Header icon grid (clean filled chips; hidden once the search panel is fully open) -----
        headerIconHitboxes.clear()
        if (!(search && searchanim.isDone)) {
            val buttonSpacing = 5f
            val gridStartX = (x + 105).toFloat()
            val buttonHeight = 21f
            val textColor = if (light) Color(18, 18, 19).rgb else -1
            var nextButtonX = gridStartX
            var buttonY = (y + 10).toFloat()

            headerIcons.forEachIndexed { index, icon ->
                if (index == 5) {
                    nextButtonX = gridStartX
                    buttonY += 24f
                }

                val buttonWidth = Fonts.Nl_18.stringWidth(icon.name) + 26f
                val isHovering = !search && RenderUtil.isHovering(nextButtonX, buttonY, buttonWidth, buttonHeight, mouseX, mouseY)
                val backgroundColor = if (light) Color(236, 238, 242) else Color(22, 23, 26)

                if (isHovering) {
                    RoundedUtil.drawRoundOutline(nextButtonX, buttonY, buttonWidth, buttonHeight, 3f, 0.5f, backgroundColor, neverlosecolor)
                } else {
                    RoundedUtil.drawRound(nextButtonX, buttonY, buttonWidth, buttonHeight, 3f, backgroundColor)
                }

                val iconFont = Fonts.nlHeaderIcon_24
                val iconGlyphWidth = iconFont.stringWidth(icon.glyph).toFloat()
                iconFont.drawString(icon.glyph, nextButtonX + 5f + (12f - iconGlyphWidth) / 2f, buttonY + 7.5f, textColor)
                Fonts.Nl_18.drawString(icon.name, nextButtonX + 22f, buttonY + 8f, textColor)

                if (!search) {
                    headerIconHitboxes.add(HeaderIconHitbox(nextButtonX, buttonY, buttonWidth, buttonHeight, icon.onClick))
                }

                nextButtonX += buttonWidth + buttonSpacing
            }
        }

        // ----- Animated search panel: slides over the icon grid, offers scope + close -----
        if (search || !searchanim.isDone) {
            val pLeft = (x + 105).toFloat()
            val pRight = (x + w - 58).toFloat()
            val pTop = (y + 8).toFloat()
            val pHeight = 50f
            val fullWidth = pRight - pLeft
            val drawWidth = fullWidth * searchProgress
            val drawLeft = pRight - drawWidth

            val panelBg = if (light) Color(236, 238, 242) else Color(15, 16, 18)
            RoundedUtil.drawRoundOutline(drawLeft, pTop, drawWidth, pHeight, 3f, 0.1f, panelBg, neverlosecolor)
            searchPanelRect = floatArrayOf(pLeft, pTop, fullWidth, pHeight)

            if (searchProgress > 0.85f) {
                val inputX = pLeft + 6f
                val inputY = pTop + 5f
                val inputW = fullWidth - 12f
                val inputH = 19f
                RoundedUtil.drawRound(inputX, inputY, inputW, inputH, 3f, if (light) Color(250, 250, 250) else Color(26, 27, 30))
                Fonts.NlIcon.nlfont_20.nlfont_20.drawString("j", inputX + 7f, inputY + 4f, neverlosecolor.rgb)
                if (searchText.isEmpty()) {
                    Fonts.Nl_16.drawString("Search modules...", inputX + 23f, inputY + 5f, (if (light) Color(150, 150, 150) else Color(120, 120, 120)).rgb)
                } else {
                    Fonts.Nl_16.drawString(searchText, inputX + 23f, inputY + 5f, if (light) Color(18, 18, 19).rgb else -1)
                }

                val closeHover = RenderUtil.isHovering(pRight - 24f, inputY, 20f, inputH, mouseX, mouseY)
                Fonts.ICONFONT_17.drawString("I", pRight - 21f, inputY + 5f, if (closeHover) Color(235, 70, 70).rgb else if (light) Color(95, 95, 95).rgb else -1)
                searchCloseRect = floatArrayOf(pRight - 24f, inputY, 20f, inputH)

                val chipY = pTop + 28f
                val chipH = 16f
                val allWidth = Fonts.Nl_16.stringWidth("Search All") + 16f
                val catWidth = Fonts.Nl_16.stringWidth("In Category") + 16f
                val allX = pLeft + 6f
                val catX = allX + allWidth + 6f

                val activeChip = neverlosecolor
                val idleChip = if (light) Color(224, 226, 230) else Color(30, 31, 34)
                RoundedUtil.drawRound(allX, chipY, allWidth, chipH, 3f, if (searchAllCategories) activeChip else idleChip)
                Fonts.Nl_16.drawString("Search All", allX + 8f, chipY + 4f, if (searchAllCategories) -1 else if (light) Color(60, 60, 60).rgb else Color(170, 170, 170).rgb)
                searchAllChipRect = floatArrayOf(allX, chipY, allWidth, chipH)

                RoundedUtil.drawRound(catX, chipY, catWidth, chipH, 3f, if (!searchAllCategories) activeChip else idleChip)
                Fonts.Nl_16.drawString("In Category", catX + 8f, chipY + 4f, if (!searchAllCategories) -1 else if (light) Color(60, 60, 60).rgb else Color(170, 170, 170).rgb)
                searchCatChipRect = floatArrayOf(catX, chipY, catWidth, chipH)
            }
        }

        if (viewerOpen) {
            espPreviewComponent.draw(mouseX, mouseY)
        }

        GlStateManager.resetColor()
        GL11.glPopMatrix()

        sideGui.drawScreen(mouseX, mouseY, partialTicks, 255)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun ensureAvatarTexture() {
        if (!avatarLoaded) {
            avatarTexture = defaultAvatar
            avatarLoaded = true
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val oldFocus = sideGui.focused
        sideGui.mouseClicked(mouseX, mouseY, mouseButton)
        if (!oldFocus) {
            nlTabs.forEach { it.click(mouseX, mouseY, mouseButton) }
            if (settings) {
                nlSetting.click(mouseX, mouseY, mouseButton)
            }
            if (viewerOpen) {
                espPreviewComponent.mouseClicked(mouseX, mouseY, mouseButton)
            }
            if (mouseButton == 0) {
                // While the search panel is open its widgets take priority over the header buttons
                if (search) {
                    if (hovering(searchCloseRect, mouseX, mouseY)) {
                        search = false
                        searchText = ""
                        dragging = false
                        return
                    }
                    if (hovering(searchAllChipRect, mouseX, mouseY)) {
                        searchAllCategories = true
                        return
                    }
                    if (hovering(searchCatChipRect, mouseX, mouseY)) {
                        searchAllCategories = false
                        return
                    }
                    if (hovering(searchPanelRect, mouseX, mouseY)) {
                        return
                    }
                }
                if (handleHeaderIconClick(mouseX, mouseY)) {
                    return
                }
                if (RenderUtil.isHovering((x + 110).toFloat(), y.toFloat(), (w - 110).toFloat(), (h - 300).toFloat(), mouseX, mouseY)) {
                    x2 = (x - mouseX)
                    y2 = (y - mouseY)
                    dragging = true
                }
                if (!search && RenderUtil.isHovering((x + 105).toFloat(), (y + 10).toFloat(), 55f, 21f, mouseX, mouseY)) {
                    if (configManager.activeConfig() != null) {
                        configManager.saveConfig(configManager.activeConfig()!!.name)
                    } else {
                        FDPClient.fileManager.saveAllConfigs()
                        configManager.refresh()
                    }
                }

                if (RenderUtil.isHovering((x + w - 50).toFloat(), (y + 17).toFloat(), Fonts.NlIcon.nlfont_20.nlfont_20.stringWidth("x").toFloat(), Fonts.NlIcon.nlfont_20.nlfont_20.height.toFloat(), mouseX, mouseY)) {
                    settings = !settings
                    dragging = false
                    nlSetting.x = x + w + 20
                    nlSetting.y = y
                }
                if (RenderUtil.isHovering((x + w - 30).toFloat(), (y + 18).toFloat(), Fonts.NlIcon.nlfont_20.nlfont_20.stringWidth("j").toFloat(), Fonts.NlIcon.nlfont_20.nlfont_20.height.toFloat(), mouseX, mouseY)) {
                    search = !search
                    dragging = false
                    if (!search) {
                        searchText = ""
                    }
                }
            }
            super.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val oldFocus = sideGui.focused
        sideGui.mouseReleased(mouseX, mouseY, state)
        if (state == 0) {
            dragging = false
        }
        if (!oldFocus) {
            nlTabs.forEach { it.released(mouseX, mouseY, state) }
            if (settings) {
                nlSetting.released(mouseX, mouseY, state)
            }
            if (viewerOpen) {
                espPreviewComponent.mouseReleased(mouseX, mouseY, state)
            }
            super.mouseReleased(mouseX, mouseY, state)
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        sideGui.keyTyped(typedChar, keyCode)
        if (search) {
            when (keyCode) {
                1 -> {
                    search = false
                    searchText = ""
                    return
                }
                14 -> {
                    if (searchText.isNotEmpty()) {
                        searchText = searchText.substring(0, searchText.length - 1)
                    }
                    return
                }
            }
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchText += typedChar
                return
            }
        }
        nlTabs.forEach { it.keyTyped(typedChar, keyCode) }
        if (viewerOpen) {
            espPreviewComponent.keyTyped(typedChar, keyCode)
        }
        super.keyTyped(typedChar, keyCode)
    }

    val isSearching: Boolean
        get() = search && searchText.isNotEmpty()

    val searchTextContent: String
        get() = searchText

    /** True when the search query should match modules from every category, not just the open one. */
    val searchScopeAll: Boolean
        get() = searchAllCategories

    /** Every module across all tabs/subcategories, used by the "Search All" scope. */
    fun allModules(): List<NlModule> = nlTabs.flatMap { tab -> tab.nlSubList }.flatMap { it.nlModules }

    private fun hovering(rect: FloatArray, mouseX: Int, mouseY: Int): Boolean =
        RenderUtil.isHovering(rect[0], rect[1], rect[2], rect[3], mouseX, mouseY)

    val light: Boolean
        get() = nlSetting.Light

    companion object {
        lateinit var INSTANCE: NeverloseGui
        var neverlosecolor = Color(28, 133, 192)
        const val HEADER_HEIGHT = 64

        fun getInstance(): NeverloseGui = INSTANCE

        @JvmStatic
        fun NLOutline(str: String, fontRenderer: FontRenderer, x: Float, y: Float, color: Int, color2: Int, size: Float) {
            fontRenderer.drawString(str, x + size, y, color2, false)
            fontRenderer.drawString(str, x, y - size, color2, false)
            fontRenderer.drawString(str, x, y, color, false)
        }
    }

    private data class HeaderIcon(val name: String, val glyph: String, val onClick: () -> Unit)

    private data class HeaderIconHitbox(val x: Float, val y: Float, val width: Float, val height: Float, val onClick: () -> Unit) {
        fun isHovering(mouseX: Int, mouseY: Int): Boolean = RenderUtil.isHovering(x, y, width, height, mouseX, mouseY)
    }

    private fun handleHeaderIconClick(mouseX: Int, mouseY: Int): Boolean {
        headerIconHitboxes.firstOrNull { it.isHovering(mouseX, mouseY) }?.let { hitbox ->
            hitbox.onClick.invoke()
            return true
        }
        return false
    }
}