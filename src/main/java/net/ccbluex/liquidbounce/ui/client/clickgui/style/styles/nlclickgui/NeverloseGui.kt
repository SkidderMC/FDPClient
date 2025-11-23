package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import com.mojang.realmsclient.gui.ChatFormatting
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.StencilUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.Configs
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.NeverloseConfigManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.BloomUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.GaussianBlur
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NeverloseGui : GuiScreen() {
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
    private val defaultAvatar = ResourceLocation(FDPClient.CLIENT_NAME.lowercase(Locale.getDefault()) + "/64.png")
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
        StencilUtil.initStencilToWrite()
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 4f, if (light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(10F)
        StencilUtil.uninitStencilBuffer()
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 2f, if (light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        RoundedUtil.drawRound((x + 90).toFloat(), (y + 40).toFloat(), (w - 90).toFloat(), (h - 40).toFloat(), 1f, if (light) Color(255, 255, 255) else Color(9, 9, 9))
        RoundedUtil.drawRound((x + 90).toFloat(), y.toFloat(), (w - 90).toFloat(), (h - 300).toFloat(), 1f, if (light) Color(255, 255, 255) else Color(13, 13, 11))
        RoundedUtil.drawRound((x + 90).toFloat(), (y + 39).toFloat(), (w - 90).toFloat(), 1f, 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))
        RoundedUtil.drawRound((x + 89).toFloat(), y.toFloat(), 1f, h.toFloat(), 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))
        GL11.glEnable(GL11.GL_BLEND)
        ensureAvatarTexture()
        mc.textureManager.bindTexture(avatarTexture)
        val footerLineY = y + h - 35
        val avatarY = footerLineY + 9
        RoundedUtil.drawRoundTextured((x + 4).toFloat(), avatarY.toFloat(), 20f, 20f, 10f, 1f)
        Fonts.Nl_18.drawString(mc.session.username, (x + 29).toFloat(), (avatarY + 1).toFloat(), if (light) Color(51, 51, 51).rgb else -1)
        Fonts.Nl_16.drawString(ChatFormatting.GRAY.toString() + "Till: " + ChatFormatting.RESET + SimpleDateFormat("dd:MM").format(Date()) + " " + SimpleDateFormat("HH:mm").format(Date()), (x + 29).toFloat(), (avatarY + 13).toFloat(), neverlosecolor.rgb)
        if (!light) {
            NLOutline("FDPCLIENT", Fonts.NLBold_28, (x + 7).toFloat(), (y + 12).toFloat(), -1, neverlosecolor.rgb, 0.7f)
        } else {
            Fonts.NLBold_28.drawString("FDP", (x + 8).toFloat(), (y + 12).toFloat(), Color(51, 51, 51).rgb, false)
        }
        RoundedUtil.drawRound(x.toFloat(), footerLineY.toFloat(), 89f, 1f, 0f, if (light) Color(213, 213, 213) else Color(26, 26, 26))
        for (nlTab in nlTabs) {
            nlTab.x = x
            nlTab.y = y
            nlTab.w = w
            nlTab.h = h
            nlTab.draw(mouseX, mouseY)
        }

        val searchProgress = searchanim.getOutput().toFloat()
        val closeButtonOffset = if (search || !searchanim.isDone()) -83f * searchProgress else 0f
        val closeButtonX = (x + w - 50 + closeButtonOffset).toFloat()
        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("x", closeButtonX, (y + 17).toFloat(), if (settings) neverlosecolor.rgb else if (light) Color(95, 95, 95).rgb else -1)

        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("j", (x + w - 30).toFloat(), (y + 18).toFloat(), if (search) neverlosecolor.rgb else if (light) Color(95, 95, 95).rgb else -1)
        searchanim.direction = if (search) Direction.FORWARDS else Direction.BACKWARDS

        if (search || !searchanim.isDone()) {
            val searchBarX = (x + w - 30 - (85f * searchProgress))
            val searchBarWidth = (80f * searchProgress)
            RoundedUtil.drawRound(searchBarX, (y + 12).toFloat(), searchBarWidth, 15f, 1f, if (light) Color(235, 235, 235) else neverlosecolor)
            val searchTextX = (x + w - 26 - (85f * searchProgress))
            Fonts.Nl_16.drawString(searchText, searchTextX, (y + 15).toFloat(), if (light) Color(18, 18, 19).rgb else -1)
        }
        if (settings) {
            nlSetting.draw(mouseX, mouseY)
        }
        RoundedUtil.drawRoundOutline((x + 105).toFloat(), (y + 10).toFloat(), 55f, 21f, 2f, 0.1f, if (light) Color(245, 245, 245) else Color(13, 13, 11), if (RenderUtil.isHovering((x + 105).toFloat(), (y + 10).toFloat(), 55f, 21f, mouseX, mouseY)) neverlosecolor else Color(19, 19, 17))
        Fonts.Nl_18.drawString("Save", (x + 128).toFloat(), (y + 18).toFloat(), if (light) Color(18, 18, 19).rgb else -1)
        Fonts.NlIcon.nlfont_20.nlfont_20.drawString("K", (x + 110).toFloat(), (y + 19).toFloat(), if (light) Color(18, 18, 19).rgb else -1)
        GL11.glPopMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun ensureAvatarTexture() {
        if (!avatarLoaded) {
            avatarTexture = defaultAvatar
            avatarLoaded = true
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        nlTabs.forEach { it.click(mouseX, mouseY, mouseButton) }
        if (settings) {
            nlSetting.click(mouseX, mouseY, mouseButton)
        }
        if (mouseButton == 0) {
            if (RenderUtil.isHovering((x + 110).toFloat(), y.toFloat(), (w - 110).toFloat(), (h - 300).toFloat(), mouseX, mouseY)) {
                x2 = (x - mouseX)
                y2 = (y - mouseY)
                dragging = true
            }
            if (RenderUtil.isHovering((x + 105).toFloat(), (y + 10).toFloat(), 55f, 21f, mouseX, mouseY)) {
                if (configManager.activeConfig() != null) {
                    configManager.saveConfig(configManager.activeConfig()!!.name)
                } else {
                    FDPClient.fileManager.saveAllConfigs()
                    configManager.refresh()
                }
            }

            val searchProgress = searchanim.getOutput().toFloat()
            val closeButtonX = (x + w - 50 + (if (search || !searchanim.isDone()) (-83f * searchProgress) else 0f))

            if (RenderUtil.isHovering(closeButtonX, (y + 17).toFloat(), Fonts.NlIcon.nlfont_24.nlfont_24.stringWidth("x").toFloat(), Fonts.NlIcon.nlfont_24.nlfont_24.height.toFloat(), mouseX, mouseY)) {
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

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        nlTabs.forEach { it.released(mouseX, mouseY, state) }
        if (state == 0) {
            dragging = false
        }
        if (settings) {
            nlSetting.released(mouseX, mouseY, state)
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
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
        super.keyTyped(typedChar, keyCode)
    }

    val isSearching: Boolean
        get() = search && searchText.isNotEmpty()

    val searchTextContent: String
        get() = searchText

    val light: Boolean
        get() = nlSetting.Light

    companion object {
        lateinit var INSTANCE: NeverloseGui
        var neverlosecolor = Color(28, 133, 192)
        @JvmStatic
        fun getInstance(): NeverloseGui = INSTANCE

        @JvmStatic
        fun NLOutline(str: String, fontRenderer: FontRenderer, x: Float, y: Float, color: Int, color2: Int, size: Float) {
            fontRenderer.drawString(str, x + size, y, color2, false)
            fontRenderer.drawString(str, x, y - size, color2, false)
            fontRenderer.drawString(str, x, y, color, false)
        }
    }
}