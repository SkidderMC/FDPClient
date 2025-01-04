/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRectWithBorder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.awt.Color

object HUDModule : Module("HUD", Category.CLIENT, defaultInArray = false, gameDetecting = false, hideModule = true) {

    val customHotbar by boolean("CustomHotbar", true)

    val smoothHotbarSlot by boolean("SmoothHotbarSlot", true) { customHotbar }

    val roundedHotbarRadius by float("RoundedHotbar-Radius", 3F, 0F..5F) { customHotbar }

    val hotbarMode by choices("Hotbar-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom") { customHotbar }
    val hbHighlightColors = ColorSettingsInteger(this, "Hotbar-Highlight-Colors", applyMax = true)
    { customHotbar }.with(a = 0)
    val hbBackgroundColors = ColorSettingsInteger(this, "Hotbar-Background-Colors")
    { customHotbar && hotbarMode == "Custom" }.with(a = 190)
    val gradientHotbarSpeed by float("Hotbar-Gradient-Speed", 1f, 0.5f..10f)
    { customHotbar && hotbarMode == "Gradient" }
    val maxHotbarGradientColors by IntegerValue("Max-Hotbar-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { customHotbar && hotbarMode == "Gradient" }
    val bgGradColors = ColorSettingsFloat.create(this, "Hotbar-Gradient")
    { customHotbar && hotbarMode == "Gradient" && it <= maxHotbarGradientColors }
    val hbHighlightBorder by float("HotbarBorder-Highlight-Width", 2F, 0.5F..5F) { customHotbar }
    val hbHighlightBorderColors = ColorSettingsInteger(this, "HotbarBorder-Highlight-Colors", zeroAlphaCheck = true)
    { customHotbar }.with(a = 255, g = 111, b = 255)
    val hbBackgroundBorder by float("HotbarBorder-Background-Width", 0.5F, 0.5F..5F) { customHotbar }
    val hbBackgroundBorderColors = ColorSettingsInteger(this, "HotbarBorder-Background-Colors", zeroAlphaCheck = true)
    { customHotbar }.with(a = 0)

    val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }
    val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }

    // InventoryPlus
    val inventoryOnHotbar = boolean("InventoryOnHotbar", false)

    // CROSSHAIR
    val csgoCrosshairValue by boolean("CSGO-Crosshair", true)

    // WATERMAKER
    private val waterMark by choices("Watemark", arrayOf("Default", "Normal", "None"), "Default")

    // UI EFFECT
    val uiEffectValue by boolean("UIEffect", true)
    val buttonShadowValue by boolean("ShadowButton", true){ uiEffectValue }
    val UiShadowValue by choices("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow") { uiEffectValue }

    private val blur by boolean("Blur", false)

    val inventoryParticle by boolean("InventoryParticle", false)

    // UI
    private val interfaceColor by boolean("Interface Color", false)
    val colorRed by int("R", 0, 0..255) { interfaceColor }
    val colorGreen by int("G", 160, 0..255) { interfaceColor }
    val colorBlue by int("B", 255, 0..255) { interfaceColor }
    private val colorRainbowValue by boolean("Rainbow", true) { interfaceColor }

    val guiColor
        get() = if (colorRainbowValue) ColorUtils.rainbow().rgb
        else Color(colorRed, colorGreen, colorBlue).rgb

    private var tickCount = 0
    private var lastSecond = System.currentTimeMillis()
    private val tpsSamples = ArrayDeque<Int>(5)
    var tps: Float = 20.0f

    val onTick = handler<GameTickEvent> {
        tickCount++

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSecond >= 1000) {
            tpsSamples.add(tickCount)
            if (tpsSamples.size > 5) {
                tpsSamples.removeFirst()
            }
            tps = tpsSamples.average().toFloat().coerceIn(0.0f, 20.0f)
            tickCount = 0
            lastSecond = currentTime
        }
    }

    val onRender2D = handler<Render2DEvent> {
        if (mc.currentScreen is GuiHudDesigner)
            return@handler

        hud.render(false)

        if (csgoCrosshairValue) {
            val screenWidth = getWindowWidth()
            val screenHeight = getWindowHeight()

            if (mc.thePlayer.isSprinting) {
                drawSprintingCrosshair(screenWidth, screenHeight)
            } else {
                drawNormalCrosshair(screenWidth, screenHeight)
            }
        }
            when (waterMark) {
                "Normal" -> {
                    val shouldChange = ColorUtils.COLOR_PATTERN.matcher(CLIENT_NAME).find()
                    val text = if (shouldChange) {
                        "§r$CLIENT_NAME"
                    } else {
                        "${CLIENT_NAME.first()}§r§f${CLIENT_NAME.substring(1)}§7[§f${Minecraft.getDebugFPS()} FPS§7]§r "
                    }

                    val color = ClientThemesUtils.getColor().rgb

                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, color)
                }
                "Default" -> {
                    val posX = 4.0f
                    val posY = 4.0f
                    val iconSize = 5.0f
                    val rectWidth = 10.0f
                    val title = "FDP"
                    val titleWidth = Fonts.InterMedium_15.stringWidth(title)
                    val bgColorRGB = ClientThemesUtils.getBackgroundColor(0, 120).rgb
                    RenderUtils.drawCustomShapeWithRadius(
                        posX,
                        posY,
                        rectWidth + iconSize * 2.5f + titleWidth,
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )
                    Fonts.Nursultan18.drawString(
                        "S",
                        posX + iconSize,
                        posY + 2 + iconSize - 1.0f + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    Fonts.InterMedium_15.drawString(
                        title,
                        posX + rectWidth + iconSize * 1.5f,
                        posY + rectWidth / 2.0f + 1.5f + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    val playerName = mc.thePlayer.name
                    val playerNameWidth = Fonts.InterMedium_15.stringWidth(playerName)
                    val playerNameX = posX + rectWidth + iconSize * 2.5f + titleWidth + iconSize
                    RenderUtils.drawCustomShapeWithRadius(
                        playerNameX,
                        posY,
                        rectWidth + iconSize * 2.5f + playerNameWidth,
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )
                    Fonts.InterMedium_15.drawString(
                        "W",
                        playerNameX + iconSize,
                        posY + 1 + iconSize + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    Fonts.InterMedium_15.drawString(
                        playerName,
                        playerNameX + iconSize * 1.5f + rectWidth,
                        posY + rectWidth / 2.0f + 1.5f + 2f,
                        -1
                    )
                    val fps = Minecraft.getDebugFPS()
                    val fpsText = "$fps FPS"
                    val fpsTextWidth = Fonts.InterMedium_15.stringWidth(fpsText)
                    val fpsX = playerNameX + rectWidth + iconSize * 2.5f + playerNameWidth + iconSize
                    RenderUtils.drawCustomShapeWithRadius(
                        fpsX,
                        posY,
                        rectWidth + iconSize * 2.5f + fpsTextWidth,
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )
                    Fonts.Nursultan18.drawString(
                        "X",
                        fpsX + iconSize,
                        posY + 1 + iconSize + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    Fonts.InterMedium_15.drawString(
                        fpsText,
                        fpsX + iconSize * 1.5f + rectWidth,
                        posY + rectWidth / 2.0f + 1.5f + 2f,
                        -1
                    )
                    val playerPosition = "${mc.thePlayer.posX.toInt()} ${mc.thePlayer.posY.toInt()} ${mc.thePlayer.posZ.toInt()}"
                    val positionTextWidth = Fonts.InterMedium_15.stringWidth(playerPosition)
                    val positionY = posY + rectWidth + iconSize * 2.0f + iconSize
                    RenderUtils.drawCustomShapeWithRadius(
                        posX,
                        positionY,
                        rectWidth + iconSize * 2.5f + positionTextWidth,
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )

                    Fonts.Nursultan18.drawString(
                        "F",
                        posX + iconSize,
                        positionY + 1.5f + iconSize + 2f,
                        ClientThemesUtils.getColor().rgb
                    )

                    Fonts.InterMedium_15.drawString(
                        playerPosition,
                        posX + iconSize * 1.5f + rectWidth,
                        positionY + rectWidth / 2.0f + 1.5f + 2f,
                        -1
                    )
                    val ping = try {
                        mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID).responseTime
                    } catch (e: Exception) {
                        0
                    }
                    val pingText = "$ping Ping"
                    val pingTextWidth = Fonts.InterMedium_15.stringWidth(pingText)
                    val pingX = posX + rectWidth + iconSize * 2.5f + positionTextWidth + iconSize
                    RenderUtils.drawCustomShapeWithRadius(
                        pingX,
                        positionY,
                        rectWidth + iconSize * 2.5f + pingTextWidth,
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )

                    Fonts.Nursultan18.drawString(
                        "Q",
                        pingX + iconSize,
                        positionY + 1 + iconSize + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    Fonts.InterMedium_15.drawString(
                        pingText,
                        pingX + iconSize * 1.5f + rectWidth,
                        positionY + rectWidth / 2.0f + 1.5f + 2f,
                        -1
                    )

                    val tpsText = "TPS: %.2f".format(tps)
                    val tpsIcon = "C"
                    val tpsX = posX
                    val tpsY = positionY + rectWidth + iconSize * 2.0f + 5f
                    RenderUtils.drawCustomShapeWithRadius(
                        tpsX,
                        tpsY,
                        rectWidth + iconSize * 2.5f + Fonts.InterMedium_15.stringWidth(tpsText),
                        rectWidth + iconSize * 2.0f,
                        4.0f,
                        Color(bgColorRGB, true)
                    )
                    Fonts.Nursultan18.drawString(
                        tpsIcon,
                        tpsX + iconSize,
                        tpsY + 1.5f + iconSize + 2f,
                        ClientThemesUtils.getColor().rgb
                    )
                    Fonts.InterMedium_15.drawString(
                        tpsText,
                        tpsX + iconSize * 1.5f + rectWidth,
                        tpsY + rectWidth / 2.0f + 1.5f + 2f,
                        -1
                    )
                }
            }
    }

    private fun drawSprintingCrosshair(screenWidth: Int, screenHeight: Int) {
        val whiteColor = Color(255, 255, 255, 255).rgb
        val borderColor = Color(0, 0, 0, 255).rgb
        val accentColor = Color(255, 190, 255, 0).rgb

        // Left part
        Gui.drawRect(screenWidth / 2 - 6, screenHeight / 2, screenWidth / 2 - 2, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 - 6).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 - 2).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7582394f, borderColor, accentColor)

        // Right part
        Gui.drawRect(screenWidth / 2 + 3, screenHeight / 2, screenWidth / 2 + 7, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 + 3).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 7).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7635231f, borderColor, accentColor)

        // Top part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 - 6, screenWidth / 2 + 1, screenHeight / 2 - 2, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 - 6).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 - 2).toFloat(), 0.7572856f, borderColor, accentColor)

        // Bottom part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 + 3, screenWidth / 2 + 1, screenHeight / 2 + 7, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 + 3).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 7).toFloat(), 0.75438696f, borderColor, accentColor)

        // Center part
        Gui.drawRect(screenWidth / 2, screenHeight / 2, screenWidth / 2 + 1, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.75438696f, borderColor, accentColor)
    }

    private fun drawNormalCrosshair(screenWidth: Int, screenHeight: Int) {
        val whiteColor = Color(255, 255, 255, 255).rgb
        val borderColor = Color(0, 0, 0, 255).rgb
        val accentColor = Color(255, 190, 255, 0).rgb

        // Left part
        Gui.drawRect(screenWidth / 2 - 9, screenHeight / 2, screenWidth / 2 - 5, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 - 9).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 - 5).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7582394f, borderColor, accentColor)

        // Right part
        Gui.drawRect(screenWidth / 2 + 6, screenHeight / 2, screenWidth / 2 + 10, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 + 6).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 10).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7635231f, borderColor, accentColor)

        // Top part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 - 9, screenWidth / 2 + 1, screenHeight / 2 - 5, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 - 9).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 - 5).toFloat(), 0.7572856f, borderColor, accentColor)

        // Bottom part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 + 7, screenWidth / 2 + 1, screenHeight / 2 + 10, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 + 7).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 10).toFloat(), 0.75438696f, borderColor, accentColor)

        // Center part
        Gui.drawRect(screenWidth / 2, screenHeight / 2, screenWidth / 2 + 1, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.75438696f, borderColor, accentColor)
    }

    private fun getWindowWidth(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).scaledWidth
    }

    private fun getWindowHeight(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).scaledHeight
    }

    val onUpdate = loopHandler {
        hud.update()
    }

    val onKey = handler<KeyEvent> { event ->
        hud.handleKey('a', event.key)
    }

    val onScreen = handler<ScreenEvent>(always = true) { event ->
        if (mc.theWorld == null || mc.thePlayer == null) return@handler
        if (state && blur && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
                !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) mc.entityRenderer.loadShader(
            ResourceLocation(CLIENT_NAME.lowercase() + "/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
            "fdpclient/blur.json" in mc.entityRenderer.shaderGroup.shaderGroupName) mc.entityRenderer.stopUseShader()
    }

    init {
        state = true
    }
}