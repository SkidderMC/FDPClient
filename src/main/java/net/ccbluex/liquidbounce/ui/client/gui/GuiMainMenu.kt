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
import net.ccbluex.liquidbounce.FDPClient.CLIENT_WEBSITE
import net.ccbluex.liquidbounce.FDPClient.clientVersionText
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.gui.multiplayer.GuiServerSelect
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
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
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiLanguage
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.GuiYesNoCallback
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

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

/**
 * Main menu laid out like the LiquidBounce NextGen launcher (icon + title + subtitle cards, account
 * card, "Latest Changes" panel, utility dock, social row) but styled to match FDPClient's native
 * buttons (Alt Manager / Font Manager): every interactive surface is a dark translucent rounded rect
 * that fills with the same cyan -> blue gradient on hover. No coloured borders. Shares the multiplayer
 * screen's font base (fontSemibold / fontRegular). Everything the legacy menu exposed is preserved.
 */
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

    private val clientPath = CLIENT_NAME.lowercase()
    private val basePath = "$clientPath/texture/mainmenu/"
    private val logo = ResourceLocation("${basePath}clickgui.png")

    private var tooltip: String? = null
    private var changelogScroll = 0f
    private var changelogMaxScroll = 0f

    private var skinName = ""
    private var skin: ResourceLocation? = null
    private var skinIsHeadTexture = false
    private var skinLoadAttemptName = ""

    private val fades = HashMap<String, Float>()

    private val margin = 16f
    private val textColor = Color(255, 255, 255).rgb
    private val dimColor = Color(176, 182, 194).rgb

    // Native-button palette (identical to MixinGuiButtonExt / Alt Manager buttons).
    private val panelBg = Color(0, 0, 0, 150)
    private val gradTop = Color.CYAN.darker()
    private val gradBottom = Color.BLUE.darker()
    private val iconBtnIdle = Color(24, 25, 30, 245)
    private val iconBtnHover = Color(0, 120, 205, 245)
    private val glyphColor = Color(150, 156, 170).rgb
    private val glyphColorHover = Color(236, 239, 245).rgb

    private val commitDate: String by lazy {
        try {
            val raw = ClientUpdate.gitInfo.getProperty("git.commit.time") ?: return@lazy ""
            val d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH).parse(raw)
            SimpleDateFormat("MMM dd", Locale.ENGLISH).format(d)
        } catch (e: Exception) { "" }
    }

    private enum class MenuIcon { PERSON, PEOPLE, LOGO, GEAR, DOC, REFRESH, EDIT, SETTINGS, EXIT, SHADER, CHEVRON }

    private data class MenuCard(
        val id: Int,
        val icon: MenuIcon,
        val title: String,
        val subtitle: String,
        val small: Boolean = false,
        val badge: String? = null
    )

    private val cards = listOf(
        MenuCard(0, MenuIcon.PERSON, "Singleplayer", "Play solo. Your world, your rules."),
        MenuCard(1, MenuIcon.PEOPLE, "Multiplayer", "Join servers and play with others."),
        MenuCard(2, MenuIcon.REFRESH, "Check Update", "Get the latest FDP build."),
        MenuCard(3, MenuIcon.GEAR, "Options", "Customize your experience.")
    )

    // Bottom utility dock – keeps every secondary action the legacy menu exposed (font-icon glyphs).
    private data class Util(val id: Int, val glyph: String, val label: String)

    private val utils = listOf(
        Util(10, "F", "Font Manager"),
        Util(11, "T", "Cosmetics"),
        Util(12, "C", "Minecraft Options"),
        Util(13, "L", "Language"),
        Util(14, "M", "Forge Mods"),
        Util(16, "Q", "ClickGUI")
    )

    private data class Social(val glyph: String, val url: String, val label: String)

    private val portfolioUrl = "https://lucas-lima.vercel.app/"

    private val socials = listOf(
        Social("G", "https://github.com/SkidderMC/FDPClient", "GitHub"),
        Social("D", "https://discord.gg/WV6qPzyqTx", "Discord"),
        Social("R", "https://www.youtube.com/@opZywl", "YouTube"),
        Social("A", portfolioUrl, "Zywl")
    )

    override fun initGui() {
        buttonList.clear()
    }

    // ---------------- GL / draw helpers (mirrors GuiServerSelect) ----------------

    private fun resetGuiState() {
        GL11.glDisable(GL11.GL_STENCIL_TEST)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun rr(x: Float, y: Float, w: Float, h: Float, radius: Float, color: Color) {
        RenderUtils.drawRoundedRect(x, y, x + w, y + h, radius, color.rgb)
        resetGuiState()
    }

    /** Dark rounded button that fills left-to-right with the cyan->blue gradient on hover (native style). */
    private fun nativeButton(x: Float, y: Float, w: Float, h: Float, radius: Float, f: Float) {
        StencilUtils.withClipping({
            RenderUtils.drawRoundedRect(x, y, x + w, y + h, radius, panelBg.rgb)
        }, {
            if (f > 0.001f) RenderUtils.drawGradientRect(x, y, x + w * f, y + h, gradTop.rgb, gradBottom.rgb, 0f)
        })
        resetGuiState()
    }

    /** Small square button (icon dock / account / social): dark idle that continuously breathes the
     *  accent in/out (travelling wave by x), and fills solid blue on hover. */
    private fun iconButton(x: Float, y: Float, size: Float, radius: Float, f: Float) {
        val idle = wave(x * 0.025f) * 0.5f
        rr(x, y, size, size, radius, mixColor(iconBtnIdle, iconBtnHover, maxOf(f, idle)))
    }

    private fun image(rl: ResourceLocation, x: Float, y: Float, w: Int, h: Int) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        RenderUtils.drawImage(rl, x, y, w, h)
        resetGuiState()
    }

    private fun anim(key: String, active: Boolean, speed: Float = 0.16f): Float {
        var f = fades[key] ?: 0f
        f = (f + (if (active) speed else -speed)).coerceIn(0f, 1f)
        fades[key] = f
        return f
    }

    private fun mixColor(a: Color, b: Color, f: Float): Color {
        val ff = f.coerceIn(0f, 1f)
        return Color(
            (a.red + (b.red - a.red) * ff).toInt().coerceIn(0, 255),
            (a.green + (b.green - a.green) * ff).toInt().coerceIn(0, 255),
            (a.blue + (b.blue - a.blue) * ff).toInt().coerceIn(0, 255),
            (a.alpha + (b.alpha - a.alpha) * ff).toInt().coerceIn(0, 255)
        )
    }

    private fun alpha(c: Color, a: Int) = Color(c.red, c.green, c.blue, a.coerceIn(0, 255))

    private fun guiText(text: CharSequence?) = text?.toString()?.replace("Â§", "§") ?: ""

    private fun drawGameText(font: GameFontRenderer, text: String, x: Float, y: Float, color: Int, shadow: Boolean = false): Int {
        resetGuiState()
        val endX = if (shadow) font.drawStringWithShadow(guiText(text), x, y, color) else font.drawString(guiText(text), x, y, color)
        resetGuiState()
        return endX
    }

    /** Default Minecraft font for every secondary label (native size). */
    private fun smallText(text: String, x: Float, y: Float, color: Int) {
        resetGuiState()
        Fonts.minecraftFont.drawStringWithShadow(guiText(text), x, y, color)
        resetGuiState()
    }

    private fun smallWidth(text: String) = Fonts.minecraftFont.getStringWidth(guiText(text))

    /** Default Minecraft font scaled up for titles/headers (crisp blocky look). Returns end x. */
    private fun titleText(text: String, x: Float, y: Float, color: Int, scale: Float = 1.45f): Float {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toDouble(), y.toDouble(), 0.0)
        GlStateManager.scale(scale.toDouble(), scale.toDouble(), 1.0)
        resetGuiState()
        Fonts.minecraftFont.drawStringWithShadow(guiText(text), 0f, 0f, color)
        GlStateManager.popMatrix()
        resetGuiState()
        return x + Fonts.minecraftFont.getStringWidth(guiText(text)) * scale
    }

    private fun pulseGreen(): Int {
        val t = (sin(System.currentTimeMillis() / 380.0) * 0.5 + 0.5).toFloat()
        return mixColor(Color(255, 255, 255), Color(78, 222, 110), t).rgb
    }

    /** Continuous travelling pulse 0..1 (phase-shifted so the accent fill sweeps in and out across a row). */
    private fun wave(phase: Float): Float {
        val t = System.currentTimeMillis() / 1000.0
        return (sin(t * 1.7 - phase) * 0.5 + 0.5).toFloat()
    }

    /** Crisp Font Awesome glyph (rebuilt fdpheadericons.ttf) centred at (cx, cy). */
    private fun drawGlyph(glyph: String, cx: Float, cy: Float, color: Int) {
        val font = Fonts.nlHeaderIcon_20
        resetGuiState()
        font.drawString(glyph, cx - font.stringWidth(glyph) / 2f, cy - font.height / 2f, color, false)
        resetGuiState()
    }

    private fun drawMenuIcon(icon: MenuIcon, x: Float, y: Float, size: Float, color: Int, alphaMul: Float = 1f) {
        resetGuiState()
        val aByte = color ushr 24 and 255
        val alpha = ((if (aByte == 0) 255 else aByte) / 255f * alphaMul).coerceIn(0f, 1f)
        val red = (color ushr 16 and 255) / 255f
        val green = (color ushr 8 and 255) / 255f
        val blue = (color and 255) / 255f

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_LINE_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth((size / 9f).coerceIn(1.3f, 2.2f))
        GL11.glColor4f(red, green, blue, alpha)

        fun px(v: Float) = x + size * v
        fun py(v: Float) = y + size * v
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            GL11.glBegin(GL11.GL_LINES)
            GL11.glVertex2f(px(x1), py(y1)); GL11.glVertex2f(px(x2), py(y2))
            GL11.glEnd()
        }
        fun poly(vararg p: Float) {
            GL11.glBegin(GL11.GL_LINE_STRIP)
            var i = 0
            while (i + 1 < p.size) { GL11.glVertex2f(px(p[i]), py(p[i + 1])); i += 2 }
            GL11.glEnd()
        }
        fun circle(cx: Float, cy: Float, r: Float, start: Int = 0, end: Int = 360) {
            GL11.glBegin(GL11.GL_LINE_STRIP)
            var deg = start
            while (deg <= end) {
                val rad = Math.toRadians(deg.toDouble())
                GL11.glVertex2f(px(cx + cos(rad).toFloat() * r), py(cy + sin(rad).toFloat() * r))
                deg += 10
            }
            GL11.glEnd()
        }

        when (icon) {
            MenuIcon.PERSON -> {
                circle(0.5f, 0.34f, 0.17f)
                poly(0.20f, 0.86f, 0.24f, 0.64f, 0.40f, 0.56f, 0.60f, 0.56f, 0.76f, 0.64f, 0.80f, 0.86f)
            }
            MenuIcon.PEOPLE -> {
                circle(0.35f, 0.36f, 0.135f)
                poly(0.12f, 0.84f, 0.16f, 0.62f, 0.30f, 0.55f, 0.50f, 0.62f, 0.54f, 0.84f)
                circle(0.68f, 0.39f, 0.12f)
                poly(0.56f, 0.82f, 0.60f, 0.62f, 0.72f, 0.56f, 0.86f, 0.62f, 0.88f, 0.80f)
            }
            MenuIcon.LOGO -> {
                poly(0.5f, 0.15f, 0.74f, 0.5f, 0.68f, 0.74f, 0.5f, 0.84f, 0.32f, 0.74f, 0.26f, 0.5f, 0.5f, 0.15f)
            }
            MenuIcon.GEAR -> {
                circle(0.5f, 0.5f, 0.19f)
                var d = 0
                while (d < 360) {
                    val rad = Math.toRadians(d.toDouble())
                    line(0.5f + cos(rad).toFloat() * 0.25f, 0.5f + sin(rad).toFloat() * 0.25f,
                        0.5f + cos(rad).toFloat() * 0.40f, 0.5f + sin(rad).toFloat() * 0.40f)
                    d += 45
                }
            }
            MenuIcon.REFRESH -> {
                circle(0.5f, 0.5f, 0.30f, 38, 322)
                line(0.80f, 0.16f, 0.86f, 0.36f)
                line(0.86f, 0.36f, 0.66f, 0.30f)
            }
            MenuIcon.DOC -> {
                poly(0.30f, 0.15f, 0.60f, 0.15f, 0.72f, 0.29f, 0.72f, 0.85f, 0.30f, 0.85f, 0.30f, 0.15f)
                poly(0.60f, 0.15f, 0.60f, 0.29f, 0.72f, 0.29f)
                line(0.38f, 0.47f, 0.64f, 0.47f)
                line(0.38f, 0.59f, 0.64f, 0.59f)
                line(0.38f, 0.71f, 0.55f, 0.71f)
            }
            MenuIcon.EDIT -> {
                poly(0.25f, 0.78f, 0.68f, 0.35f, 0.78f, 0.45f, 0.35f, 0.88f, 0.22f, 0.91f, 0.25f, 0.78f)
                line(0.62f, 0.29f, 0.74f, 0.17f)
                line(0.74f, 0.17f, 0.86f, 0.29f)
                line(0.86f, 0.29f, 0.78f, 0.45f)
            }
            MenuIcon.SETTINGS -> {
                line(0.18f, 0.32f, 0.82f, 0.32f); circle(0.62f, 0.32f, 0.085f)
                line(0.18f, 0.5f, 0.82f, 0.5f); circle(0.36f, 0.5f, 0.085f)
                line(0.18f, 0.68f, 0.82f, 0.68f); circle(0.66f, 0.68f, 0.085f)
            }
            MenuIcon.EXIT -> {
                circle(0.5f, 0.56f, 0.30f, 35, 305)
                line(0.5f, 0.16f, 0.5f, 0.5f)
            }
            MenuIcon.SHADER -> {
                line(0.5f, 0.18f, 0.5f, 0.82f)
                line(0.18f, 0.5f, 0.82f, 0.5f)
                line(0.29f, 0.29f, 0.71f, 0.71f)
                line(0.71f, 0.29f, 0.29f, 0.71f)
            }
            MenuIcon.CHEVRON -> {
                line(0.40f, 0.28f, 0.62f, 0.5f)
                line(0.62f, 0.5f, 0.40f, 0.72f)
            }
        }

        GL11.glPopAttrib()
        resetGuiState()
    }

    private fun playerSkin(): ResourceLocation {
        val name = mc.session.username
        if (name != skinName || skin == null) {
            skinName = name
            skinIsHeadTexture = false
            skinLoadAttemptName = ""
            val profile = mc.session.profile
            val fallbackId = profile.id ?: UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8))
            skin = DefaultPlayerSkin.getDefaultSkin(fallbackId)
        }
        loadPlayerHead(name)
        return skin ?: DefaultPlayerSkin.getDefaultSkin(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8)))
    }

    private fun loadPlayerHead(name: String) {
        if (skinLoadAttemptName == name || name.isBlank()) return
        skinLoadAttemptName = name
        Thread({
            try {
                val encodedName = URLEncoder.encode(name, "UTF-8")
                val image = URL("https://mc-heads.net/avatar/$encodedName/64").openStream().use { ImageIO.read(it) } ?: return@Thread
                val resourceName = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_").lowercase()
                val rl = ResourceLocation("fdpplayerhead/$resourceName")
                mc.addScheduledTask {
                    mc.textureManager.loadTexture(rl, DynamicTexture(image))
                    if (skinName == name) {
                        skin = rl
                        skinIsHeadTexture = true
                    }
                }
            } catch (ignored: Exception) {
            }
        }, "FDP Player Head Loader").apply { isDaemon = true }.start()
    }

    // ---------------- layout ----------------

    private val cardX get() = margin + 8f
    private val cardW get() = (width * 0.285f).coerceIn(214f, 290f)
    private val cardBigH = 44f
    private val cardSmallH = 32f
    private val cardGap = 10f
    private val cardsTop get() = (height * 0.205f).coerceAtLeast(margin + 70f)

    private fun cardBounds(index: Int): FloatArray {
        var y = cardsTop
        for (i in 0 until index) y += (if (cards[i].small) cardSmallH else cardBigH) + cardGap
        val h = if (cards[index].small) cardSmallH else cardBigH
        return floatArrayOf(cardX, y, cardW, h)
    }

    private val accountW get() = (width * 0.28f).coerceIn(204f, 262f)
    private val accountH = 50f
    private val accountX get() = width - margin - accountW
    private val accountY = margin

    private val bottomRowH = 22f
    private val exitW = 58f
    private val configW = 122f
    private val socialSlot = 24f
    private val bottomY get() = height - margin - bottomRowH

    // ---------------- rendering ----------------

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        tooltip = null
        assumeNonVolatile = true
        drawBackground(0)
        resetGuiState()

        if (popup != null) {
            popup?.drawScreen(width, height, mouseX, mouseY)
            assumeNonVolatile = false
            return
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            openClickGui()
            return
        }

        drawTopBar()
        drawCards(mouseX, mouseY)
        drawAccountCard(mouseX, mouseY)
        drawChangelogPanel(mouseX, mouseY)
        drawBottomBar(mouseX, mouseY)
        drawUtilityDock(mouseX, mouseY)
        drawSocial(mouseX, mouseY)

        val branch = FDPClient.clientBranch
        val commitIdAbbrev = ClientUpdate.gitInfo.getProperty("git.commit.id.abbrev")
        val infoStr = "$CLIENT_NAME ($branch/$commitIdAbbrev) | Minecraft 1.8.9"
        smallText(infoStr, margin + 8f, bottomY - 13f, Color(255, 255, 255, 140).rgb)

        tooltip?.let { drawTooltip(it, mouseX, mouseY) }

        assumeNonVolatile = false
    }

    private fun drawStatusPill(text: String, x: Float, y: Float, dotColor: Color?, textColorInt: Int): Float {
        val tw = smallWidth(text)
        val w = tw + (if (dotColor != null) 17f else 11f)
        rr(x, y, w, 13f, 3.5f, Color(255, 255, 255, 26))
        var tx = x + 5.5f
        if (dotColor != null) { rr(x + 5.5f, y + 4f, 4.5f, 4.5f, 2.25f, dotColor); tx = x + 13f }
        smallText(text, tx, y + 3f, textColorInt)
        return x + w
    }

    private fun drawTopBar() {
        val lw = 54f
        val lh = 58f
        image(logo, margin, margin, lw.toInt(), lh.toInt())
        val tx = margin + lw + 10f
        smallText("made by Zywl", tx + 1f, margin + lh / 2f - 17f, Color(190, 194, 204).rgb)
        drawStatusPill(clientVersionText, tx, margin + lh / 2f - 2f, null, Color(222, 226, 234).rgb)
    }

    private fun drawCards(mouseX: Int, mouseY: Int) {
        val noPopup = popup == null
        for (card in cards) {
            val b = cardBounds(card.id)
            val x = b[0]; val y = b[1]; val w = b[2]; val h = b[3]
            val hovered = noPopup && RenderHelper.isHovering(x, y, w, h, mouseX, mouseY)
            val f = maxOf(anim("card${card.id}", hovered), wave(card.id * 1.15f) * 0.42f)

            nativeButton(x, y, w, h, 4f, f)

            // neutral icon disc + white icon
            val disc = if (card.small) h - 12f else h - 16f
            val discX = x + 9f
            val discY = y + (h - disc) / 2f
            rr(discX, discY, disc, disc, disc / 2f, mixColor(Color(255, 255, 255, 22), Color(255, 255, 255, 50), f))
            if (card.icon == MenuIcon.LOGO) {
                image(logo, discX + disc * 0.18f, discY + disc * 0.18f, (disc * 0.64f).toInt(), (disc * 0.64f).toInt())
            } else {
                drawMenuIcon(card.icon, discX + disc * 0.24f, discY + disc * 0.24f, disc * 0.52f, textColor)
            }

            val textX = discX + disc + 11f
            if (card.small) {
                drawGameText(Fonts.fontSemibold35, card.title, textX, y + h / 2f - 5f, textColor)
                if (card.badge != null) {
                    val bw = smallWidth(card.badge) + 10f
                    val bx = textX + Fonts.fontSemibold35.getStringWidth(card.title) + 9f
                    rr(bx, y + h / 2f - 6f, bw, 12f, 3f, Color(255, 255, 255, 32))
                    smallText(card.badge, bx + 5f, y + h / 2f - 3f, Color(96, 220, 130).rgb)
                }
            } else {
                val titleColor = if (card.id == 2) pulseGreen() else textColor
                val tEnd = titleText(card.title, textX, y + 9f, titleColor)
                if (card.id == 2 && commitDate.isNotEmpty()) {
                    val bt = "Beta · $commitDate"
                    val bw = smallWidth(bt) + 12f
                    rr(tEnd + 9f, y + 9f, bw, 13f, 3f, Color(78, 200, 110, 55))
                    smallText(bt, tEnd + 15f, y + 11.5f, Color(120, 226, 150).rgb)
                }
                smallText(card.subtitle, textX, y + h - 12f, dimColor)
            }

            drawMenuIcon(MenuIcon.CHEVRON, x + w - 20f, y + h / 2f - 6.5f, 13f, alpha(Color.WHITE, (110 + 145 * f).toInt().coerceAtMost(255)).rgb)
        }
    }

    private fun drawAccountCard(mouseX: Int, mouseY: Int) {
        val x = accountX; val y = accountY; val cw = accountW; val ch = accountH
        rr(x, y, cw, ch, 8f, panelBg)
        rr(x + 1.2f, y + 1.2f, cw - 2.4f, ch - 2.4f, 7f, Color(255, 255, 255, 12))

        val avSize = 34f
        val avX = x + 9f
        val avY = y + (ch - avSize) / 2f
        val playerTexture = playerSkin()
        resetGuiState()
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (skinIsHeadTexture) image(playerTexture, avX, avY, avSize.toInt(), avSize.toInt())
        else RenderUtils.drawHead(playerTexture, avX.toInt(), avY.toInt(), avSize.toInt(), avSize.toInt(), -1)
        resetGuiState()

        val nameX = avX + avSize + 9f
        titleText(mc.session.username, nameX, y + 11f, textColor, 1.3f)
        val premium = (mc.session.token?.length ?: 0) > 5 && !"0".equals(mc.session.token, true)
        rr(nameX, y + ch - 15f, 5f, 5f, 2.5f, if (premium) Color(103, 210, 74) else Color(150, 154, 162))
        smallText(if (premium) "Microsoft" else "Offline", nameX + 9f, y + ch - 15f, if (premium) Color(103, 210, 74).rgb else dimColor)

        val isz = 22f
        val setX = x + cw - isz - 8f
        val penX = setX - isz - 6f
        val iy = y + (ch - isz) / 2f
        val penHov = RenderHelper.isHovering(penX, iy, isz, isz, mouseX, mouseY)
        val setHov = RenderHelper.isHovering(setX, iy, isz, isz, mouseX, mouseY)
        iconButton(penX, iy, isz, 5f, anim("acpen", penHov))
        drawMenuIcon(MenuIcon.EDIT, penX + 5f, iy + 5f, 12f, textColor)
        if (penHov) tooltip = "Alt Manager"
        iconButton(setX, iy, isz, 5f, anim("acset", setHov))
        drawMenuIcon(MenuIcon.SETTINGS, setX + 5f, iy + 5f, 12f, textColor)
        if (setHov) tooltip = "Minecraft Options"
    }

    private fun changelogX() = (accountX - 4f)
    private fun changelogW() = (accountW + 4f).coerceAtLeast(220f)
    private fun changelogY() = accountY + accountH + 12f
    private fun changelogH() = (height * 0.44f).coerceIn(150f, 296f)

    private fun drawChangelogPanel(mouseX: Int, mouseY: Int) {
        val x = changelogX(); val w = changelogW(); val y = changelogY(); val h = changelogH()
        rr(x, y, w, h, 8f, panelBg)
        rr(x + 1.2f, y + 1.2f, w - 2.4f, h - 2.4f, 7f, Color(255, 255, 255, 10))

        titleText("Latest Changes", x + 14f, y + 12f, textColor, 1.3f)
        val totalChanges = changelogs.split("\n").count { it.isNotBlank() && !it.trim().startsWith("#") }
        if (totalChanges > 0) {
            val bt = "Changes: $totalChanges"
            val bw = smallWidth(bt) + 12f
            rr(x + w - bw - 14f, y + 11f, bw, 13f, 3.5f, Color(255, 255, 255, 22))
            smallText(bt, x + w - bw - 8f, y + 14f, dimColor)
        }

        // API + build status (moved here from the top bar)
        val online = canConnect
        var stx = x + 14f
        val sty = y + 30f
        stx = drawStatusPill(if (online) "API Online" else "API Offline", stx, sty,
            if (online) Color(74, 200, 110) else Color(224, 78, 78), Color(214, 218, 226).rgb) + 6f
        val latest = canConnect && isLatest
        val buildLabel = when {
            canConnect && isLatest -> "Latest"
            !canConnect -> "API Dead"
            else -> "Outdated"
        }
        drawStatusPill(buildLabel, stx, sty, null, (if (latest) Color(96, 220, 130) else Color(236, 176, 80)).rgb)

        val btnH = 22f
        val btnY = y + h - btnH - 12f
        val listTop = y + 50f
        val listBottom = btnY - 8f
        val viewH = listBottom - listTop
        val entries = changelogEntries()
        val rowH = 17f
        val contentH = entries.size * rowH
        changelogMaxScroll = (contentH - viewH).coerceAtLeast(0f)
        changelogScroll = changelogScroll.coerceIn(0f, changelogMaxScroll)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderHelper.scissor(x.toDouble(), listTop.toDouble(), w.toDouble(), viewH.toDouble())
        val dot = Color(120, 160, 210)
        var ey = listTop - changelogScroll
        for (entry in entries) {
            if (ey + rowH >= listTop && ey <= listBottom) {
                rr(x + 15f, ey + 4f, 5f, 5f, 2.5f, dot)
                smallText(trimToWidth(entry, w - 46f), x + 27f, ey + 1f, Color(216, 220, 228).rgb)
            }
            ey += rowH
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        resetGuiState()
        if (entries.isEmpty()) smallText("No changelog available.", x + 16f, listTop, dimColor)

        if (changelogMaxScroll > 0f) {
            val sbH = (viewH * (viewH / contentH)).coerceAtLeast(16f)
            val sbY = listTop + (changelogScroll / changelogMaxScroll) * (viewH - sbH)
            rr(x + w - 4.5f, sbY, 2.5f, sbH, 1.25f, Color(255, 255, 255, 80))
        }

        val btnHov = RenderHelper.isHovering(x + 14f, btnY, w - 28f, btnH, mouseX, mouseY)
        nativeButton(x + 14f, btnY, w - 28f, btnH, 4f, anim("changelogBtn", btnHov))
        val label = "View Full Changelog"
        val lw = smallWidth(label)
        smallText(label, x + 14f + (w - 28f - lw) / 2f, btnY + btnH / 2f - 4f, textColor)
    }

    private fun changelogEntries(): List<String> {
        return changelogs.split("\n")
            .map { it.trim().removePrefix("~").removePrefix("+").removePrefix("-").removePrefix("*").trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .take(40)
    }

    private fun trimToWidth(text: String, maxWidth: Float): String {
        if (smallWidth(text) <= maxWidth) return text
        var r = text
        while (r.isNotEmpty() && smallWidth("$r…") > maxWidth) r = r.dropLast(1)
        return "$r…"
    }

    private fun drawBottomBar(mouseX: Int, mouseY: Int) {
        val by = bottomY
        val bh = bottomRowH

        val exitHov = RenderHelper.isHovering(margin, by, exitW, bh, mouseX, mouseY)
        rr(margin, by, exitW, bh, 5f, mixColor(Color(40, 18, 18, 175), Color(206, 62, 62, 245), maxOf(anim("exit", exitHov), wave(margin * 0.025f) * 0.4f)))
        drawMenuIcon(MenuIcon.EXIT, margin + 9f, by + bh / 2f - 6.5f, 13f, textColor)
        smallText("Exit", margin + 27f, by + bh / 2f - 4f, textColor)

        val cx = margin + exitW + 8f
        val cfgHov = RenderHelper.isHovering(cx, by, configW, bh, mouseX, mouseY)
        nativeButton(cx, by, configW, bh, 5f, maxOf(anim("cfg", cfgHov), wave(cx * 0.025f) * 0.42f))
        drawGlyph("Z", cx + 13f, by + bh / 2f, glyphColorHover)
        smallText("Configuration", cx + 24f, by + bh / 2f - 4f, textColor)
    }

    private fun utilDockBounds(): FloatArray {
        val isz = bottomRowH
        val gap = 7f
        val total = utils.size * isz + (utils.size - 1) * gap
        val startX = (width - total) / 2f
        return floatArrayOf(startX, bottomY, isz, gap)
    }

    private fun drawUtilityDock(mouseX: Int, mouseY: Int) {
        val d = utilDockBounds()
        var x = d[0]; val y = d[1]; val isz = d[2]; val gap = d[3]
        for (u in utils) {
            val hov = RenderHelper.isHovering(x, y, isz, isz, mouseX, mouseY)
            iconButton(x, y, isz, 5f, anim("util${u.id}", hov))
            drawGlyph(u.glyph, x + isz / 2f, y + isz / 2f, if (hov) glyphColorHover else glyphColor)
            if (hov) tooltip = u.label
            x += isz + gap
        }
    }

    private fun socialWebW() = (Fonts.nlHeaderIcon_20.stringWidth("O") + smallWidth(CLIENT_WEBSITE) + 20).toFloat()

    private fun socialContainerW() = 6f + socials.size * socialSlot + 6f + socialWebW() + 6f

    private fun socialBounds(): FloatArray {
        val w = socialContainerW()
        return floatArrayOf(width - margin - w, bottomY, w, bottomRowH)
    }

    private fun drawSocial(mouseX: Int, mouseY: Int) {
        val b = socialBounds()
        val cx = b[0]; val cy = b[1]; val cw = b[2]; val ch = b[3]
        rr(cx, cy, cw, ch, 6f, panelBg)
        var ix = cx + 6f
        for (social in socials) {
            val hov = RenderHelper.isHovering(ix, cy, socialSlot, ch, mouseX, mouseY)
            if (hov) rr(ix + 1f, cy + 3f, socialSlot - 2f, ch - 6f, 4f, iconBtnHover)
            drawGlyph(social.glyph, ix + socialSlot / 2f, cy + ch / 2f, if (hov) glyphColorHover else glyphColor)
            if (hov) tooltip = social.label
            ix += socialSlot
        }
        // website pill inside the same container (external-link + domain)
        val wx = ix + 6f
        val webW = socialWebW()
        val hov = RenderHelper.isHovering(wx, cy + 3f, webW, ch - 6f, mouseX, mouseY)
        rr(wx, cy + 3f, webW, ch - 6f, 4f, mixColor(Color(0, 116, 200, 235), Color(0, 150, 235, 255), maxOf(anim("socweb", hov), wave(wx * 0.025f) * 0.55f)))
        drawGlyph("O", wx + 12f, cy + ch / 2f, Color.WHITE.rgb)
        smallText(CLIENT_WEBSITE, wx + 22f, cy + ch / 2f - 4f, Color.WHITE.rgb)
        if (hov) tooltip = "Website"
    }

    private fun drawTooltip(text: String, mouseX: Int, mouseY: Int) {
        val w = smallWidth(text) + 12f
        val tx = (mouseX - w / 2f).coerceIn(4f, width - w - 4f)
        val ty = (mouseY - 20f).coerceAtLeast(4f)
        rr(tx, ty, w, 15f, 3.5f, Color(8, 8, 12, 240))
        smallText(text, tx + 6f, ty + 4f, textColor)
    }

    // ---------------- input ----------------

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (popup != null) {
            popup!!.mouseClicked(mouseX, mouseY, button)
            return
        }
        if (button != 0) return

        if (RenderHelper.isHovering(margin + 65f, margin + 12f, smallWidth("made by Zywl").toFloat(), 10f, mouseX, mouseY)) {
            MiscUtils.showURL(portfolioUrl); return
        }

        for (card in cards) {
            val b = cardBounds(card.id)
            if (RenderHelper.isHovering(b[0], b[1], b[2], b[3], mouseX, mouseY)) {
                onAction(card.id)
                return
            }
        }

        val isz = 22f
        val setX = accountX + accountW - isz - 8f
        val penX = setX - isz - 6f
        val iy = accountY + (accountH - isz) / 2f
        if (RenderHelper.isHovering(penX, iy, isz, isz, mouseX, mouseY)) { mc.displayGuiScreen(GuiAltManager(this)); return }
        if (RenderHelper.isHovering(setX, iy, isz, isz, mouseX, mouseY)) { mc.displayGuiScreen(GuiOptions(this, mc.gameSettings)); return }
        if (RenderHelper.isHovering(accountX, accountY, accountW - 56f, accountH, mouseX, mouseY)) { mc.displayGuiScreen(GuiAltManager(this)); return }

        val cx = changelogX(); val cw = changelogW(); val cy = changelogY(); val chH = changelogH()
        val cBtnH = 24f
        val cBtnY = cy + chH - cBtnH - 12f
        if (RenderHelper.isHovering(cx + 14f, cBtnY, cw - 28f, cBtnH, mouseX, mouseY)) {
            MiscUtils.showURL("https://github.com/SkidderMC/FDPClient/commits/main")
            return
        }

        val by = bottomY
        val bh = bottomRowH
        if (RenderHelper.isHovering(margin, by, exitW, bh, mouseX, mouseY)) { mc.shutdown(); return }
        if (RenderHelper.isHovering(margin + exitW + 8f, by, configW, bh, mouseX, mouseY)) { mc.displayGuiScreen(GuiClientConfiguration(this)); return }

        val d = utilDockBounds()
        var ux = d[0]; val uy = d[1]; val usz = d[2]; val ugap = d[3]
        for (u in utils) {
            if (RenderHelper.isHovering(ux, uy, usz, usz, mouseX, mouseY)) { onAction(u.id); return }
            ux += usz + ugap
        }

        val sb = socialBounds()
        var ix = sb[0] + 6f; val sy = sb[1]; val sh = sb[3]
        for (social in socials) {
            if (RenderHelper.isHovering(ix, sy, socialSlot, sh, mouseX, mouseY)) { MiscUtils.showURL(social.url); return }
            ix += socialSlot
        }
        if (RenderHelper.isHovering(ix + 6f, sy + 3f, socialWebW(), sh - 6f, mouseX, mouseY)) { MiscUtils.showURL("https://fdpinfo.github.io/next/"); return }
    }

    private fun onAction(id: Int) {
        when (id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiServerSelect(this))
            2 -> mc.displayGuiScreen(GuiUpdate())
            3 -> mc.displayGuiScreen(GuiInfo(this))
            4 -> mc.displayGuiScreen(GuiCommitInfo())
            10 -> mc.displayGuiScreen(GuiFontManager(this))
            11 -> mc.displayGuiScreen(GuiCommitInfo())
            12 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            13 -> mc.displayGuiScreen(GuiLanguage(this, mc.gameSettings, mc.languageManager))
            14 -> mc.displayGuiScreen(GuiModList(this))
            16 -> openClickGui()
        }
    }

    private fun openClickGui() {
        try {
            mc.displayGuiScreen(ClickGui)
        } catch (e: Exception) {
            e.printStackTrace()
            ClickGui.initGui()
            mc.displayGuiScreen(ClickGui)
        }
    }

    override fun actionPerformed(button: GuiButton) {
        if (popup != null) return
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiServerSelect(this))
            2 -> mc.displayGuiScreen(GuiInfo(this))
            3 -> mc.displayGuiScreen(GuiFontManager(this))
            4 -> mc.displayGuiScreen(GuiUpdate())
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
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        if (wheel == 0) return
        if (popup != null) popup!!.handleMouseWheel(wheel)
        else changelogScroll = (changelogScroll - wheel / 8f).coerceIn(0f, changelogMaxScroll)
    }
}
