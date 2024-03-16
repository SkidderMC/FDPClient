/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.visual.CustomClientColor
import net.ccbluex.liquidbounce.ui.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.newVer.NewUi
import net.ccbluex.liquidbounce.ui.gui.menu.GuiTeleportation
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils.getPing
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.BlurUtils.blurAreaRounded
import net.ccbluex.liquidbounce.utils.render.RenderUtils.width
import net.ccbluex.liquidbounce.utils.render.ShadowUtils.shadow
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.network.play.client.C14PacketTabComplete
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S3APacketTabComplete
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.EnumChatFormatting
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUD : Module() {

    // WaterMark
    private val waterMark = BoolValue("Watermark", true)
    val modeValue = ListValue("Watermark-Mode", arrayOf("FDPCLIENT", "Classic", "FDP", "Blur", "Clean", "Zywl", "ZAVZ", "Slide"), "FDPCLIENT").displayable { waterMark.get() }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Health", "Theme"), "Health").displayable { waterMark.get() }

    // CrossHair
    val crossHairValue = BoolValue("CrossHair", false)
    val tpDebugValue = BoolValue("TP-Debug", false)
    val nof5crossHair = BoolValue("NoF5-CrossHair", true)

    // MISC
    private val noInvClose = BoolValue("NoInvClose", false)
    private val noTitle = BoolValue("NoTitle", false)
    private val antiTabComplete = BoolValue("AntiTabComplete", false)
    val noAchievement = BoolValue("No-Achievements", true)

    // UI EFFECT
    private val uiEffectValue = BoolValue("UIEffect", true)
    val buttonShadowValue = BoolValue("ShadowButton", true).displayable  { uiEffectValue.get() }
    val UiShadowValue = ListValue("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow").displayable  { uiEffectValue.get() }
    val inventoryParticle = BoolValue("InventoryParticle", false).displayable  { uiEffectValue.get() }

    // CAMERA
    private val viewValue = BoolValue("BetterView", true)
    val smoothCamera = BoolValue("Smooth", true).displayable  { viewValue.get() }
    val cameraPositionValue = BoolValue("CameraPosition", false).displayable  { viewValue.get() }
    val cameraPositionYawValue = FloatValue("Yaw", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionPitchValue = FloatValue("Pitch", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionFovValue = FloatValue("DistanceFov", 4F, 1F, 50F).displayable  { cameraPositionValue.get() }

    // Fonts
    val shadowValue = ListValue("TextShadowMode", arrayOf("Normal", "LiquidBounce", "Outline", "Default", "Autumn"), "Normal")
    private val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)

    private var lastFontEpsilon = 0f
    /**
     * Renders the HUD.
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        FDPClient.hud.render(false, event.partialTicks)
        if (waterMark.get()) renderWatermark()

        if (crossHairValue.get()) {
            val lrs1 = width()
            val lrs2 = RenderUtils.height()
            if (mc.thePlayer.isSprinting) {
                Gui.drawRect(lrs1 / 2 - 3 - 3, lrs2 / 2, lrs1 / 2 + 6 - 8, lrs2 / 2 + 2 - 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 - 6).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 - 2).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7582394f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2 + 6 - 3, lrs2 / 2, lrs1 / 2 + 10 - 3, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 3).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 7).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7635231f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 - 6, lrs1 / 2 + 1, lrs2 / 2 - 2, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 1 - 1).toFloat(), (lrs2 / 2 - 6).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 - 2).toFloat(), 0.7572856f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 + 6 - 3, lrs1 / 2 + 1, lrs2 / 2 + 7, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 + 6 - 3).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 10 - 3).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2, lrs1 / 2 + 1, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
            } else {
                Gui.drawRect(lrs1 / 2 + 10 - 19, lrs2 / 2, lrs1 / 2 + 5 - 10, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 - 9).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 - 5).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7582394f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2 + 6, lrs2 / 2, lrs1 / 2 + 10, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 6).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 5 + 5).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7635231f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 - 9, lrs1 / 2 + 1, lrs2 / 2 - 5, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 - 9).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 - 5).toFloat(), 0.7572856f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 + 2 + 4, lrs1 / 2 + 1, lrs2 / 2 + 10, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 + 3 + 3).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 3 + 7).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2, lrs1 / 2 + 1, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (noTitle.get() && event.packet is S45PacketTitle) {
            event.cancelEvent()
        }

        if (antiTabComplete.get() && (event.packet is C14PacketTabComplete || event.packet is S3APacketTabComplete)) {
            event.cancelEvent()
        }

        if (mc.theWorld == null || mc.thePlayer == null) return
        if (noInvClose.get() && event.packet is S2EPacketCloseWindow && (mc.currentScreen is GuiInventory || mc.currentScreen is NewUi || mc.currentScreen is ClickGui || mc.currentScreen is GuiChat || mc.currentScreen is GuiTeleportation)) {
            event.cancelEvent()
        }
    }

    /**
     * Renders the watermark.
     */
    private fun renderWatermark() {
        val dateFormat = SimpleDateFormat("HH:mm")

        val name = FDPClient.CLIENT_NAME
        when (modeValue.get()) {
            "FDPCLIENT" -> {
                var width = ""
                val other = ""
                val leagth = Fonts.fontTenacityBold40.getStringWidth(name) + Fonts.fontTenacityBold35.getStringWidth(other)
                RenderUtils.customRounded(2F, 3.5F, leagth + 6F, Fonts.fontTenacityBold40.FONT_HEIGHT + 5F, 0F, 0F, 5F, 5F, Color(0,0,0,180).rgb)
                RenderUtils.drawAnimatedGradient(2.0, 3.0, leagth + 6.0, 4.0, ClientTheme.getColor(0).rgb, ClientTheme.getColor(90).rgb)
                GlowUtils.drawGlow(3.79F, 6.07F, 3.83F + Fonts.fontTenacityBold40.getStringWidth(name).toFloat(), 7.21F, 9, ClientTheme.getColor(1))
                for (l in name.indices) {
                    Fonts.fontTenacityBold40.drawString(name[l].toString(), 5F + Fonts.fontTenacityBold40.getStringWidth(width).toFloat(), 5.5F, ClientTheme.getColor(l * -135).rgb, true)
                    width += name[l].toString()
                }
                Fonts.fontTenacityBold35.drawString(other, Fonts.fontTenacityBold40.getStringWidth("FDP").toFloat() + 5F, 6.5F, Color(255,255,255).rgb)
                GlStateManager.resetColor()
            }

            "Classic" -> {
                var width = ""
                val name = "FDP"
                val other = " | ${FDPClient.USER_NAME} | ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                val leagth = Fonts.fontTenacityBold40.getStringWidth(name) + Fonts.fontTenacityBold35.getStringWidth(other)
                RenderUtils.customRounded(2F, 3.5F, leagth + 6F, Fonts.fontTenacityBold40.FONT_HEIGHT + 5F, 0F, 0F, 5F, 5F, Color(0,0,0,180).rgb)
                RenderUtils.drawAnimatedGradient(2.0, 3.0, leagth + 6.0, 4.0, ClientTheme.getColor(0).rgb, ClientTheme.getColor(90).rgb)
                GlowUtils.drawGlow(3.79F, 6.07F, 3.83F + Fonts.fontTenacityBold40.getStringWidth(name).toFloat(), 7.21F, 9, ClientTheme.getColor(1))
                for (l in name.indices) {
                    Fonts.fontTenacityBold40.drawString(name[l].toString(), 5F + Fonts.fontTenacityBold40.getStringWidth(width).toFloat(), 5.5F, ClientTheme.getColor(l * -135).rgb, true)
                    width += name[l].toString()
                }
                Fonts.fontTenacityBold35.drawString(other, Fonts.fontTenacityBold40.getStringWidth("FDP").toFloat() + 5F, 6.5F, Color(255,255,255).rgb)
                GlStateManager.resetColor()
            }

            "Zywl" -> {
                val title = String.format(
                    "| %s | %s | %sfps",
                    FDPClient.CLIENT_VERSION,
                    mc.session.username,
                    Minecraft.getDebugFPS()
                )
                val mark = name
                val width = (Fonts.font35.getStringWidth(title) + Fonts.font35.getStringWidth(mark) + 6).toFloat()
                RenderUtils.drawExhiRect(8.0f, 8.0f, width + 10.0f, (Fonts.font40.height + 8).toFloat(), 1f)
                Fonts.font35.drawString(mark, 8.5f, 10f, getColor().rgb, true)
                Fonts.font35.drawString(
                    title,
                    (13.5 + Fonts.font35.getStringWidth(mark)).toFloat(), 9.0f, -1
                )
            }

            "FDP" -> {
                val text =
                    EnumChatFormatting.DARK_GRAY.toString() + "   |  " + EnumChatFormatting.WHITE + mc.thePlayer.name + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE + getPing(
                        mc.thePlayer
                    ) + "ms" + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE + dateFormat.format(
                        Date()
                    ) + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE

                shadow(15f, {
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderUtils.originalRoundedRect(
                        6f,
                        7f,
                        (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontSFUI35.getStringWidth(name)).toFloat(),
                        27f,
                        5.4f,
                        getColor().rgb
                    )
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                }, {
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderUtils.originalRoundedRect(
                        6f,
                        7f,
                        (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontSFUI35.getStringWidth(name)).toFloat(),
                        27f,
                        5.4f,
                        getColor().rgb
                    )
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                })
                blurAreaRounded(
                    6f,
                    7f,
                    (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontSFUI35.getStringWidth(name)).toFloat(),
                    27f,
                    5.4f,
                    10f
                )
                RenderUtils.drawRoundedRect(
                    6f,
                    7f,
                    (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontSFUI35.getStringWidth(name)).toFloat(),
                    27f,
                    5.4f,
                    Color(0, 0, 0, 100).rgb
                )

                Fonts.fontSFUI35.drawStringWithShadow(" $name", 8f, 13f, -1)
                Fonts.fontSFUI35.drawString(text, 7 + Fonts.fontSFUI35.getStringWidth(name), 13, Color.WHITE.rgb)
            }

            "Blur" -> {
                val text =
                    EnumChatFormatting.DARK_GRAY.toString() + "   |  " + EnumChatFormatting.WHITE + mc.thePlayer.name + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE + getPing(
                        mc.thePlayer
                    ) + "ms" + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE + dateFormat.format(
                        Date()
                    ) + EnumChatFormatting.DARK_GRAY + "  |  " + EnumChatFormatting.WHITE
                shadow(15f, {
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderUtils.originalRoundedRect(
                        6f,
                        7f,
                        (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name)).toFloat(),
                        27f,
                        5.4f,
                        getColor().rgb
                    )
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                }, {
                    GlStateManager.enableBlend()
                    GlStateManager.disableTexture2D()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderUtils.originalRoundedRect(
                        6f,
                        7f,
                        (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name)).toFloat(),
                        27f,
                        5.4f,
                        getColor().rgb
                    )
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                })
                blurAreaRounded(
                    6f,
                    7f,
                    (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name)).toFloat(),
                    27f,
                    5.4f,
                    10f
                )
                RenderUtils.drawRoundedRect(
                    6f,
                    7f,
                    (5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name)).toFloat(),
                    27f,
                    5.4f,
                    Color(0, 0, 0, 100).rgb
                )
                val c1 = interpolateColorsBackAndForth(6, 90, getColor(), getColor(), false)
                val c2 = interpolateColorsBackAndForth(6, 180, getColor(), getColor(), false)
                Fonts.fontTahoma.drawString(" $name", 10, 12, Color(0, 0, 0).rgb)
                RoundedUtil.applyGradientHorizontal(
                    10f,
                    12f,
                    Fonts.fontTahoma.getStringWidth("  $name").toFloat(),
                    Fonts.fontTahoma.height.toFloat(),
                    1f,
                    c1,
                    c2
                ) {
                    RenderUtils.setAlphaLimit(0f)
                    Fonts.fontTahoma.drawString(
                        " $name",
                        10,
                        12,
                        Color(255, 255, 255).rgb
                    )
                    GlowUtils.drawGlow(
                        10f,
                        12f,
                        Fonts.fontTahoma.getStringWidth(" $name").toFloat(),
                        Fonts.fontTahoma.height.toFloat(),
                        6,
                        applyOpacity(
                            Color(
                                255,
                                255,
                                255
                            ), 0.5f
                        )
                    )
                }

                Fonts.fontSFUI35.drawString(text, 9 + Fonts.fontTahoma.getStringWidth(name), 13, Color.WHITE.rgb)
            }

            "ZAVZ" -> {
                val username = mc.thePlayer.name
                val servername = if (mc.isSingleplayer) "Singleplayer" else mc.currentServerData.serverIP
                val times = dateFormat.format(Date())
                RoundedUtil.drawRound(
                    6f, 5f, (Fonts.fontTahoma.getStringWidth(name) + Fonts.fontTahoma30.getStringWidth(
                        " | $username | $servername | $times"
                    ) + 3 + 5).toFloat(), 12f, 1f, Color(0, 0, 0, 100)
                )
                Fonts.fontTahoma.drawString(name, 9, 7, Color(24, 114, 165).rgb)
                Fonts.fontTahoma.drawString(name, 8, 7, -1)
                Fonts.fontTahoma30.drawString(
                    " | $username | $servername | $times",
                    Fonts.fontTahoma.getStringWidth(name) + 11,
                    8,
                    -1
                )
            }

            "Slide" -> {
                drawNewRect(
                    5.0,
                    6.0,
                    (Fonts.fontSFUI40.getStringWidth(name) + 7).toDouble(),
                    19.0,
                    Color(25, 125, 255).rgb
                )
                drawNewRect(
                    7.0,
                    6.0,
                    (Fonts.fontSFUI40.getStringWidth(name) + 10).toDouble(),
                    19.0,
                    Color(31, 31, 31).rgb
                )
                Fonts.fontSFUI40.drawString(name, 8f, 9f, -1, true)
            }

            "Clean" -> {
                val append5 = name.substring(0, 1)
                val append6 = name.substring(1)

                val clientname = append5 + EnumChatFormatting.WHITE + append6

                val username2 = mc.thePlayer.name
                val servername2 = if (mc.isSingleplayer) "Singleplayer" else mc.currentServerData.serverIP
                val fps2 = Minecraft.getDebugFPS().toString() + "fps"
                val time2 = dateFormat.format(Date())
                val y = -1
                val x = -4

                val watermarkfont = Fonts.fontSFUI35

                val watermarkfont2 = Fonts.fontSFUI40

                RoundedUtil.drawRound(
                    (10 + x).toFloat(), (5 + y).toFloat(), watermarkfont.getStringWidth(username2) +
                            watermarkfont.getStringWidth(clientname) +
                            watermarkfont.getStringWidth(servername2) +
                            watermarkfont.getStringWidth(fps2) +
                            watermarkfont.getStringWidth(time2) +
                            (watermarkfont2.getStringWidth(" | ") * 4)
                            + 2.5f, 11.5f, 0f, Color(0, 0, 0, 90)
                )

                RoundedUtil.drawRound(
                    (10 + x).toFloat(), 4.3f + y, watermarkfont.getStringWidth(username2) +
                            watermarkfont.getStringWidth(clientname) +
                            watermarkfont.getStringWidth(servername2) +
                            watermarkfont.getStringWidth(fps2) +
                            watermarkfont.getStringWidth(time2) +
                            (watermarkfont2.getStringWidth(" | ") * 4)
                            + 2.5f, 0.7f, 0f, Color(getColor().rgb)
                )

                watermarkfont.drawString(clientname, 11f + x, 9f + y, getColor().rgb)

                watermarkfont2.drawString(
                    " | ",
                    (11 + x + watermarkfont.getStringWidth(clientname)).toFloat(),
                    8f + y,
                    getColor().rgb,
                    false
                )

                watermarkfont.drawString(
                    username2, 11f + x + watermarkfont.getStringWidth(clientname)
                            + watermarkfont2.getStringWidth(" | "),
                    9f + y, -1
                )

                watermarkfont2.drawString(
                    " | ", (11 + x + watermarkfont.getStringWidth(username2) +
                            watermarkfont.getStringWidth(clientname) +
                            watermarkfont2.getStringWidth(" | ")).toFloat(), 8f + y, getColor().rgb, false
                )

                watermarkfont.drawString(
                    servername2, (11f + x + watermarkfont.getStringWidth(clientname)
                            + watermarkfont.getStringWidth(username2)) + watermarkfont2.getStringWidth(" | ") * 2,
                    9f + y, -1
                )

                watermarkfont2.drawString(
                    " | ",
                    (11 + x + watermarkfont.getStringWidth(username2) +
                            watermarkfont.getStringWidth(clientname) +
                            watermarkfont.getStringWidth(servername2) + watermarkfont2.getStringWidth(" | ") * 2).toFloat(),
                    8f + y,
                    getColor().rgb,
                    false
                )

                watermarkfont.drawString(
                    fps2, (11f + x + watermarkfont.getStringWidth(clientname)
                            + watermarkfont.getStringWidth(servername2)
                            + watermarkfont.getStringWidth(username2)) + watermarkfont2.getStringWidth(" | ") * 3,
                    9f + y, -1
                )

                watermarkfont2.drawString(
                    " | ",
                    (11 + x + watermarkfont.getStringWidth(username2) +
                            watermarkfont.getStringWidth(fps2) +
                            watermarkfont.getStringWidth(clientname) +
                            watermarkfont.getStringWidth(servername2) + watermarkfont2.getStringWidth(" | ") * 3).toFloat(),
                    8f + y,
                    getColor().rgb,
                    false
                )

                watermarkfont.drawString(
                    time2, (11f + x + watermarkfont.getStringWidth(clientname)
                            + watermarkfont.getStringWidth(fps2)
                            + watermarkfont.getStringWidth(servername2)
                            + watermarkfont.getStringWidth(username2)) + watermarkfont2.getStringWidth(" | ") * 4,
                    9f + y, -1
                )
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        FDPClient.hud.update()
        if (mc.currentScreen == null && lastFontEpsilon != fontEpsilonValue.get()) {
            lastFontEpsilon = fontEpsilonValue.get()
            alert("You need to reload FDPClient to apply changes!")
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastFontEpsilon = fontEpsilonValue.get()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent?) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return
        }

        if (state && mc.entityRenderer.isShaderActive) {
            mc.entityRenderer.stopUseShader()
        }
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        FDPClient.hud.handleKey('a', event.key)
    }

    fun getColor(): Color {
        return when (colorModeValue.get()) {
            "Custom" -> CustomClientColor.getColor()
            "Rainbow" -> ClientTheme.getColor(1)
            else -> Color.white
        }
    }

    private fun drawNewRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 0xFF).toFloat() / 255.0f
        val f = (color shr 16 and 0xFF).toFloat() / 255.0f
        val f1 = (color shr 8 and 0xFF).toFloat() / 255.0f
        val f2 = (color and 0xFF).toFloat() / 255.0f
        val tessellator: Tessellator = Tessellator.getInstance()
        val vertexbuffer: WorldRenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.color(f, f1, f2, f3)
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(left, bottom, 0.0).endVertex()
        vertexbuffer.pos(right, bottom, 0.0).endVertex()
        vertexbuffer.pos(right, top, 0.0).endVertex()
        vertexbuffer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun color(tick: Int): Color {
        var textColor = Color(-1)
        textColor = ColorUtils.fade(5, tick * 20, getColor(), 1.0f)
        return textColor
    }

    private fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        min(1.0, max(0.0, amount.toDouble())).toFloat().also { amount = it }
        return Color(
            MathUtils.interpolateInt(color1.red, color2.red, amount.toDouble()),
            MathUtils.interpolateInt(color1.green, color2.green, amount.toDouble()),
            MathUtils.interpolateInt(color1.blue, color2.blue, amount.toDouble()),
            MathUtils.interpolateInt(color1.alpha, color2.alpha, amount.toDouble())
        )
    }

    private fun interpolateColorsBackAndForth(speed: Int, index: Int, start: Color, end: Color, trueColor: Boolean): Color {
        var angle = (((System.currentTimeMillis()) / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        return if (trueColor) interpolateColorHue(start, end, angle / 360f) else interpolateColorC(
            start,
            end,
            angle / 360f
        )
    }

    private fun interpolateColorHue(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()

        val color1HSB = Color.RGBtoHSB(color1.red, color1.green, color1.blue, null)
        val color2HSB = Color.RGBtoHSB(color2.red, color2.green, color2.blue, null)

        val resultColor = Color.getHSBColor(
            MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount.toDouble()),
            MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount.toDouble()), MathUtils.interpolateFloat(
                color1HSB[2],
                color2HSB[2], amount.toDouble()
            )
        )

        return Color(
            resultColor.red, resultColor.green, resultColor.blue,
            MathUtils.interpolateInt(color1.alpha, color2.alpha, amount.toDouble())
        )
    }

    private fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1.0, max(0.0, opacity.toDouble())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }


}
