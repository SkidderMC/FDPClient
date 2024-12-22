/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui

import net.ccbluex.liquidbounce.FDPClient.fileManager
import net.ccbluex.liquidbounce.config.SettingsUtils.applyScript
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.generateColor
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.api.ClientApi
import net.ccbluex.liquidbounce.handler.api.ClientApi.getSettingsScript
import net.ccbluex.liquidbounce.handler.api.autoSettingsList
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.ClientColorMode
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.ThemeFadeSpeed
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColorFromName
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.updown
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.interpolateFloat
import net.ccbluex.liquidbounce.utils.render.AnimationUtils.animate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.math.max
import kotlin.math.min

class SideGui : GuiPanel() {

    private val categories = arrayOf("UI", "Configs", "Color")

    var focused = false
    private var clickAnimation: Animation? = null
    private var hoverAnimation: Animation? = null
    private var textAnimation: Animation? = null
    private var moveOverGradientAnimation: Animation? = null
    private var categoryAnimation = HashMap<String, Array<Animation>>()
    private var drag: Drag? = null

    private var currentCategory = "UI"
    private var scroll = 0f
    private var animScroll = 0f

    private val smooth = floatArrayOf(0f, 0f, 0f, 0f)
    private var timerUtil: TimerUtil? = null

    private var showLocalConfigs = false
    private var wasMousePressed = false

    // If we're dragging the fade-speed slider, no panel-drag or scrolling is allowed
    private var draggingSlider = false

    // Also block window-drag if the user is clicking on any header button (UI, Configs, Color)
    private var clickingHeader = false

    // Outline around the entire side panel
    private var showSideOutline = true

    override fun initGui() {
        focused = false

        rectWidth = 550f
        rectHeight = 350f

        val sr = ScaledResolution(MinecraftInstance.mc)
        timerUtil = TimerUtil()
        drag = Drag(
            x = (sr.scaledWidth - 30).toFloat(),
            y = sr.scaledHeight / 2f - rectHeight / 2f
        )

        textAnimation = DecelerateAnimation(500, 1.0).apply {
            setDirection(Direction.BACKWARDS)
        }
        clickAnimation = DecelerateAnimation(325, 1.0).apply {
            setDirection(Direction.BACKWARDS)
        }
        hoverAnimation = DecelerateAnimation(250, 1.0).apply {
            setDirection(Direction.BACKWARDS)
        }
        moveOverGradientAnimation = DecelerateAnimation(250, 1.0).apply {
            setDirection(Direction.BACKWARDS)
        }

        // Animations for each category
        categoryAnimation.clear()
        for (cat in categories) {
            categoryAnimation[cat] = arrayOf(
                DecelerateAnimation(250, 1.0),
                DecelerateAnimation(250, 1.0)
            )
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // We don't need key events here
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float, alpha: Int) {
        assumeNonVolatile = true

        // If not dragging the slider or clicking header, allow scroll
        if (!draggingSlider && !clickingHeader) {
            handleMouseWheel()
        }

        animScroll = animate(animScroll, scroll, 0.5f)
        updateAnimations(mouseX, mouseY)

        val sr = ScaledResolution(MinecraftInstance.mc)
        val mainRectColor = drawMainPanel(sr, alpha, mouseX, mouseY)

        // Draw category tabs (centered)
        drawCategoryTabs(mouseX, mouseY, alpha)

        // Separator line below the header
        DrRenderUtils.drawRect2(
            drag!!.x + 20.0,
            drag!!.y + 50.0,
            (rectWidth - 40).toDouble(),
            1.0,
            Color(45, 45, 45, alpha).rgb
        )

        // Category content
        when (currentCategory) {
            "UI" -> drawUiCategory(alpha)
            "Configs" -> drawConfigsCategory(mouseX, mouseY, alpha)
            "Color" -> drawColorCategory(mouseX, mouseY, alpha)
        }

        // Overlays, gradients, bloom, etc.
        drawOverlays(sr, alpha, mouseX, mouseY)

        assumeNonVolatile = false
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val isHoveringMainRect = DrRenderUtils.isHovering(drag!!.x, drag!!.y, rectWidth, rectHeight, mouseX, mouseY)
        if (isHoveringMainRect && button == 0 && !focused) {
            focused = true
            return
        }

        if (!focused) return

        // If user clicked on any header button, block drag
        clickingHeader = isHoveringHeader(mouseX, mouseY)

        // Only drag if not dragging slider or clicking header
        if (!draggingSlider && !clickingHeader) {
            // Window dragging
            val canDrag = DrRenderUtils.isHovering(drag!!.x, drag!!.y, rectWidth, 50f, mouseX, mouseY) ||
                    DrRenderUtils.isHovering(drag!!.x, drag!!.y, 20f, rectHeight, mouseX, mouseY)
            drag!!.onClick(mouseX, mouseY, button, canDrag)
        }

        // Check if a category tab was clicked
        checkCategoryClick(mouseX, mouseY)

        // If we're in the "Color" category, check for slider or color interactions
        if (currentCategory == "Color") {
            checkColorCategoryInteractions(mouseX, mouseY)
        }

        // If user clicked on fade-speed slider
        val fadeSpeedSliderX = drag!!.x + 25
        val fadeSpeedSliderY = drag!!.y + 20
        val fadeSpeedSliderWidth = 80f
        val fadeSpeedSliderHeight = 10f
        if (
            DrRenderUtils.isHovering(
                fadeSpeedSliderX,
                fadeSpeedSliderY,
                fadeSpeedSliderWidth,
                fadeSpeedSliderHeight,
                mouseX,
                mouseY
            ) && button == 0
        ) {
            draggingSlider = true
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        if (focused) {
            // Stop dragging
            drag!!.onRelease(button)

            // If dragged outside the right edge, close the SideGui
            val sr = ScaledResolution(MinecraftInstance.mc)
            if (drag!!.x + rectWidth > sr.scaledWidth && clickAnimation!!.isDone) {
                focused = false
            }
        }
        // Stop dragging slider
        draggingSlider = false
        // Stop blocking the header
        clickingHeader = false
    }

    /**
     * Check if a category tab was clicked and update [currentCategory] if so.
     */
    private fun checkCategoryClick(mouseX: Int, mouseY: Int) {
        val totalWidth = 3 * 60f + 2 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15

        var xOffset = 0f
        categories.forEach { cat ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(
                xVal - 30, yVal - 5,
                60f,
                (Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.height + 10).toFloat(),
                mouseX, mouseY
            )
            if (hovered) {
                currentCategory = cat
                return
            }
            xOffset += 60f + 10f
        }
    }

    /**
     * Check if user interacted with the "Side" toggle or fadeSpeed slider in the "Color" category.
     */
    private fun checkColorCategoryInteractions(mouseX: Int, mouseY: Int) {
        val buttonX = drag!!.x + 25 + (80f + 10) * 5
        val buttonY = drag!!.y + 60
        val buttonW = 80f
        val buttonH = 20f
        val hoveredToggle = DrRenderUtils.isHovering(buttonX, buttonY, buttonW, buttonH, mouseX, mouseY)
        if (hoveredToggle) {
            updown = !updown
        }

        val sliderX = drag!!.x + 25
        val sliderY = drag!!.y + 20
        val sliderW = 80f
        val sliderH = 10f
        val hoveredSlider = DrRenderUtils.isHovering(sliderX, sliderY, sliderW, sliderH, mouseX, mouseY)
        if (hoveredSlider) {
            var newSpeed = ((mouseX - sliderX) / sliderW) * 10
            newSpeed = max(0f, min(10f, newSpeed))
            ThemeFadeSpeed = newSpeed.toInt()
        }
    }

    private fun handleMouseWheel() {
        val wheel = Mouse.getDWheel()
        if (wheel != 0) {
            scroll += if (wheel > 0) -30f else 30f
            scroll = max(-200f, min(0f, scroll))
        }
    }

    private fun updateAnimations(mouseX: Int, mouseY: Int) {
        clickAnimation?.setDirection(if (focused) Direction.FORWARDS else Direction.BACKWARDS)

        val hovering = DrRenderUtils.isHovering(drag!!.x, drag!!.y, rectWidth, rectHeight, mouseX, mouseY)
        hoverAnimation?.setDirection(if (hovering) Direction.FORWARDS else Direction.BACKWARDS)

        val sr = ScaledResolution(MinecraftInstance.mc)
        val stillAnimating = !timerUtil!!.hasTimeElapsed(6000) ||
                (!hoverAnimation!!.isDone || (hoverAnimation!!.isDone && hoverAnimation!!.direction == Direction.FORWARDS))

        textAnimation?.setDirection(if (!focused && stillAnimating) Direction.FORWARDS else Direction.BACKWARDS)

        if (!clickAnimation!!.isDone) {
            drag!!.x = interpolateFloat(
                sr.scaledWidth - 30f,
                if (focused) sr.scaledWidth / 2f - rectWidth / 2f else drag!!.x,
                clickAnimation!!.output.toFloat().toDouble()
            )
            drag!!.y = interpolateFloat(
                sr.scaledHeight / 2f - rectHeight / 2f,
                drag!!.y,
                clickAnimation!!.output.toFloat().toDouble()
            )
        }

        val exceedingRightEdge = drag!!.x + rectWidth > sr.scaledWidth &&
                (focused && clickAnimation!!.isDone && clickAnimation!!.direction == Direction.FORWARDS)
        moveOverGradientAnimation?.setDirection(if (exceedingRightEdge) Direction.FORWARDS else Direction.BACKWARDS)
    }

    /**
     * Draw the main panel with optional outline.
     */
    private fun drawMainPanel(sr: ScaledResolution, alpha: Int, mouseX: Int, mouseY: Int): Color {
        val hoverOut = hoverAnimation?.output ?: 0.0
        val clickOut = clickAnimation?.output ?: 0.0
        val moveOut = moveOverGradientAnimation?.output ?: 0.0

        var rectAlpha = min(
            (185 + 30 * hoverOut + 70 * clickOut).toFloat() - (70 * moveOut).toFloat(),
            255f
        )
        rectAlpha *= alpha / 255f

        val mainRectColor = Color(30, 30, 30, rectAlpha.toInt())

        if (focused) {
            // Drag the window if not blocked
            if (!draggingSlider && !clickingHeader) {
                drag!!.onDraw(mouseX, mouseY)
            }
        }

        // Draw the main rectangle
        drawCustomShapeWithRadius(drag!!.x, drag!!.y, rectWidth, rectHeight, 9f, mainRectColor)

        // Outline around the entire SideGui if showSideOutline is true
        if (showSideOutline) {
            val outlineColor = generateColor(0)
            val thickness = 1f
            drawRoundedOutline(
                drag!!.x,
                drag!!.y,
                drag!!.x + rectWidth,
                drag!!.y + rectHeight,
                9f,
                thickness,
                outlineColor.rgb
            )
        }

        return mainRectColor
    }

    /**
     * Draw the header tabs (UI, Configs, Color) in a centered way.
     */
    private fun drawCategoryTabs(mouseX: Int, mouseY: Int, alpha: Int) {
        val textColor = DrRenderUtils.applyOpacity(-1, alpha / 255f)
        val totalWidth = 3 * 60f + 2 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15

        var xOffset = 0f
        categories.forEachIndexed { index, cat ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(
                xVal - 30, yVal - 5,
                60f,
                (Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.height + 10).toFloat(),
                mouseX, mouseY
            )

            val catHoverAnim = categoryAnimation[cat]?.get(0)
            val catEnableAnim = categoryAnimation[cat]?.get(1)

            catHoverAnim?.setDirection(if (hovered) Direction.FORWARDS else Direction.BACKWARDS)
            catEnableAnim?.setDirection(if (currentCategory == cat) Direction.FORWARDS else Direction.BACKWARDS)

            val baseColor = Color(45, 45, 45, alpha)
            val colorToInterpolate = DrRenderUtils.applyOpacity(generateColor(index).rgb, alpha / 255f)

            val hoverOut = catHoverAnim?.output?.toFloat() ?: 0f
            val enableOut = catEnableAnim?.output?.toFloat() ?: 0f

            val colorToInterpolateAsColor = Color(colorToInterpolate, true)

            val hoverColor: Color = DrRenderUtils.interpolateColorC(
                baseColor,
                DrRenderUtils.brighter(baseColor, 0.8f),
                hoverOut
            )
            val finalColor: Color = DrRenderUtils.interpolateColorC(hoverColor, colorToInterpolateAsColor, enableOut)

            drawCustomShapeWithRadius(
                xVal - 30, yVal - 5,
                60f,
                (Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.height + 10).toFloat(),
                6f,
                finalColor
            )

            Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawCenteredString(cat, xVal, yVal, textColor)
            xOffset += 60f + 10f
        }
    }

    /**
     * Draw "UI" category content.
     */
    private fun drawUiCategory(alpha: Int) {
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "Not Finished - Coming Soon",
            drag!!.x + rectWidth / 2,
            drag!!.y + rectHeight / 2,
            DrRenderUtils.applyOpacity(-1, alpha / 255f)
        )
    }

    /**
     * Draw the "Configs" category content (ONLINE or LOCAL).
     */
    private fun drawConfigsCategory(mouseX: Int, mouseY: Int, alpha: Int) {
        val buttonToggleWidth = 70f
        val buttonToggleHeight = 20f
        val buttonSpacing = 10f

        val xStart = drag!!.x + 25
        // Place "OPEN FOLDER" above the "ONLINE"/"LOCAL" buttons
        val openFolderButtonWidth = buttonToggleWidth * 2
        val openFolderButtonX = xStart
        val openFolderButtonY = drag!!.y + 30
        val isOpenFolderHovered = DrRenderUtils.isHovering(
            openFolderButtonX, openFolderButtonY,
            openFolderButtonWidth, buttonToggleHeight,
            mouseX, mouseY
        )
        val openFolderButtonColor = if (isOpenFolderHovered) Color(70, 70, 70, alpha).rgb else Color(50, 50, 50, alpha).rgb
        DrRenderUtils.drawRect2(
            openFolderButtonX.toDouble(),
            openFolderButtonY.toDouble(),
            openFolderButtonWidth.toDouble(),
            buttonToggleHeight.toDouble(),
            openFolderButtonColor
        )
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "OPEN FOLDER",
            openFolderButtonX + 10,
            openFolderButtonY + 5,
            DrRenderUtils.applyOpacity(-1, alpha / 255f)
        )

        // "ONLINE" and "LOCAL" below
        val onlineButtonX = xStart
        val onlineButtonY = openFolderButtonY + buttonToggleHeight + buttonSpacing
        val isOnlineHovered = DrRenderUtils.isHovering(
            onlineButtonX, onlineButtonY,
            buttonToggleWidth, buttonToggleHeight,
            mouseX, mouseY
        )
        val onlineButtonColor = when {
            !showLocalConfigs -> Color(100, 150, 100, alpha).rgb
            isOnlineHovered   -> Color(70, 70, 70, alpha).rgb
            else              -> Color(50, 50, 50, alpha).rgb
        }
        DrRenderUtils.drawRect2(
            onlineButtonX.toDouble(), onlineButtonY.toDouble(),
            buttonToggleWidth.toDouble(), buttonToggleHeight.toDouble(),
            onlineButtonColor
        )
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "ONLINE",
            onlineButtonX + 10,
            onlineButtonY + 5,
            DrRenderUtils.applyOpacity(-1, alpha / 255f)
        )

        val localButtonX = onlineButtonX + buttonToggleWidth + buttonSpacing
        val localButtonY = onlineButtonY
        val isLocalHovered = DrRenderUtils.isHovering(
            localButtonX, localButtonY,
            buttonToggleWidth, buttonToggleHeight,
            mouseX, mouseY
        )
        val localButtonColor = when {
            showLocalConfigs -> Color(100, 150, 100, alpha).rgb
            isLocalHovered   -> Color(70, 70, 70, alpha).rgb
            else             -> Color(50, 50, 50, alpha).rgb
        }
        DrRenderUtils.drawRect2(
            localButtonX.toDouble(), localButtonY.toDouble(),
            buttonToggleWidth.toDouble(), buttonToggleHeight.toDouble(),
            localButtonColor
        )
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "LOCAL",
            localButtonX + 10,
            localButtonY + 5,
            DrRenderUtils.applyOpacity(-1, alpha / 255f)
        )

        // Mouse click detection for the above buttons
        if (!wasMousePressed && Mouse.isButtonDown(0)) {
            when {
                isOpenFolderHovered -> openFolder()
                isOnlineHovered     -> showLocalConfigs = false
                isLocalHovered      -> showLocalConfigs = true
            }
            wasMousePressed = true
        }
        if (!Mouse.isButtonDown(0)) wasMousePressed = false

        // Now draw the actual config list
        drawConfigList(mouseX, mouseY, alpha, localButtonY + buttonToggleHeight + buttonSpacing)
    }

    /**
     * Adjusted config list, starting below the toggle buttons.
     */
    private fun drawConfigList(mouseX: Int, mouseY: Int, alpha: Int, startY: Float) {
        var configX = drag!!.x + 25
        var configY = startY
        val buttonWidth = (rectWidth - 50) / 4 - 10
        val buttonHeight = 20f
        val configsPerRow = 4
        var configCount = 0

        if (showLocalConfigs) {
            val localConfigs = fileManager.settingsDir.listFiles { _, name -> name.endsWith(".txt") }
            if (!localConfigs.isNullOrEmpty()) {
                for (file in localConfigs) {
                    drawSingleConfigButton(mouseX, mouseY, alpha, configX, configY, buttonWidth, buttonHeight) {
                        val configName = file.name.removeSuffix(".txt")
                        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
                            configName,
                            configX + 5,
                            configY + 5,
                            DrRenderUtils.applyOpacity(-1, alpha / 255f)
                        )
                        if (
                            DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY)
                            && Mouse.isButtonDown(0)
                        ) {
                            loadLocalConfig(configName, file)
                        }
                    }
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = drag!!.x + 25
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
                    "No local configurations available.",
                    configX,
                    configY,
                    DrRenderUtils.applyOpacity(-1, alpha / 255f)
                )
            }
        } else {
            // Online configs
            if (!autoSettingsList.isNullOrEmpty()) {
                for (i in autoSettingsList!!.indices) {
                    val autoSetting = autoSettingsList!![i]
                    drawSingleConfigButton(mouseX, mouseY, alpha, configX, configY, buttonWidth, buttonHeight) {
                        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
                            autoSetting.name,
                            configX + 5,
                            configY + 5,
                            DrRenderUtils.applyOpacity(-1, alpha / 255f)
                        )
                        if (
                            DrRenderUtils.isHovering(configX, configY, buttonWidth, buttonHeight, mouseX, mouseY)
                            && Mouse.isButtonDown(0)
                        ) {
                            loadOnlineConfig(autoSetting.settingId, autoSetting.name)
                        }
                    }
                    configX += buttonWidth + 10
                    configCount++
                    if (configCount % configsPerRow == 0) {
                        configX = drag!!.x + 25
                        configY += buttonHeight + 5
                    }
                }
            } else {
                Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
                    "No online configurations available.",
                    configX,
                    configY,
                    DrRenderUtils.applyOpacity(-1, alpha / 255f)
                )
            }
        }
    }

    private inline fun drawSingleConfigButton(
        mouseX: Int,
        mouseY: Int,
        alpha: Int,
        configX: Float,
        configY: Float,
        width: Float,
        height: Float,
        drawContent: () -> Unit
    ) {
        val isHovered = DrRenderUtils.isHovering(configX, configY, width, height, mouseX, mouseY)
        val buttonColor = if (isHovered) Color(70, 70, 70, alpha).rgb else Color(50, 50, 50, alpha).rgb
        DrRenderUtils.drawRect2(
            configX.toDouble(),
            configY.toDouble(),
            width.toDouble(),
            height.toDouble(),
            buttonColor
        )
        drawContent()
    }

    private fun loadLocalConfig(configName: String, file: File) {
        try {
            displayChatMessage("Loading local configuration: $configName...")
            val localConfigContent = Files.readAllBytes(file.toPath()).toString(StandardCharsets.UTF_8)
            applyScript(localConfigContent)
            displayChatMessage("Local configuration $configName loaded successfully!")
        } catch (e: IOException) {
            displayChatMessage("Error loading local configuration: ${e.message}")
        }
    }

    /**
     * FIXED HERE: Now we pass ("legacy", settingId) in the correct order.
     */
    private fun loadOnlineConfig(settingId: String, configName: String) {
        try {
            displayChatMessage("Loading configuration: $configName...")
            val configScript = ClientApi.getSettingsScript("legacy", settingId)
            applyScript(configScript)
            displayChatMessage("Configuration $configName loaded successfully!")
        } catch (e: Exception) {
            displayChatMessage("Error loading configuration: ${e.message}")
        }
    }

    /**
     * Draw "Color" category content.
     */
    private fun drawColorCategory(mouseX: Int, mouseY: Int, alpha: Int) {
        val themeColors = arrayOf(
            "Zywl", "Water", "Magic", "DarkNight", "Sun",
            "Tree", "Flower", "Loyoi", "Cero", "Soniga",
            "May", "Mint", "Azure", "Rainbow", "Astolfo",
            "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
            "Fire", "Aqua", "Peony"
        )

        val colorXStart = drag!!.x + 25
        val colorYStart = drag!!.y + 60 + animScroll
        val colorWidth = 80f
        val colorHeight = 60f
        val colorsPerRow = 5
        var colorX = colorXStart
        var colorY = colorYStart

        val maxVisibleHeight = drag!!.y + rectHeight - 60

        themeColors.forEachIndexed { i, colorName ->
            if (colorY + colorHeight > drag!!.y + 60 && colorY < maxVisibleHeight) {
                val isHovered = DrRenderUtils.isHovering(colorX, colorY, colorWidth, colorHeight, mouseX, mouseY)
                if (isHovered && Mouse.isButtonDown(0)) {
                    ClientColorMode = colorName
                    fileManager.saveConfig(fileManager.colorThemeConfig, true)
                    LOGGER.info("Saved color theme configuration: $colorName")
                }

                val startColor = getColorFromName(colorName, 0).rgb
                val endColor = getColorFromName(colorName, 180).rgb
                drawGradientRect(
                    colorX.toInt(),
                    colorY.toInt(),
                    (colorX + colorWidth).toInt(),
                    (colorY + colorHeight).toInt(),
                    startColor, endColor
                )

                val isSelected = (ClientColorMode == colorName)
                if (isSelected) {
                    smooth[0] = animate(smooth[0], colorX, 0.02f * deltaTime)
                    smooth[1] = animate(smooth[1], colorY, 0.02f * deltaTime)
                    smooth[2] = animate(smooth[2], colorX + colorWidth, 0.02f * deltaTime)
                    smooth[3] = animate(smooth[3], colorY + colorHeight, 0.02f * deltaTime)
                    drawRoundedOutline(
                        smooth[0],
                        smooth[1],
                        smooth[2],
                        smooth[3],
                        10f,
                        3f,
                        Color(startColor).brighter().rgb
                    )
                }

                Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawCenteredString(
                    colorName,
                    colorX + colorWidth / 2f,
                    colorY + colorHeight / 2f - Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.height / 2,
                    Color.WHITE.rgb
                )
            }
            colorX += colorWidth + 10
            if ((i + 1) % colorsPerRow == 0) {
                colorX = colorXStart
                colorY += colorHeight + 10
            }
        }

        drawColorExtras(mouseX, mouseY, alpha, colorXStart, drag!!.y + 60, colorWidth)
    }

    /**
     * Draw fade-speed slider and toggle button for the "Color" category, pinned at a fixed Y.
     */
    private fun drawColorExtras(
        mouseX: Int,
        mouseY: Int,
        alpha: Int,
        colorXStart: Float,
        buttonBaseY: Float,
        colorWidth: Float
    ) {
        val buttonX = colorXStart + (colorWidth + 10) * 5
        val buttonY = buttonBaseY
        val buttonWidth = 50f
        val buttonHeight = 15f
        val fadeSpeedSliderX = drag!!.x + 25
        val fadeSpeedSliderY = drag!!.y + 20
        val fadeSpeedSliderWidth = 80f
        val fadeSpeedSliderHeight = 10f

        // Fade speed slider background
        DrRenderUtils.drawRect2(
            fadeSpeedSliderX.toDouble(),
            fadeSpeedSliderY.toDouble(),
            fadeSpeedSliderWidth.toDouble(),
            fadeSpeedSliderHeight.toDouble(),
            Color(60, 60, 60).rgb
        )

        // Filled portion of the slider
        val sliderValue = (ThemeFadeSpeed / 10f) * fadeSpeedSliderWidth
        DrRenderUtils.drawRect2(
            fadeSpeedSliderX.toDouble(),
            fadeSpeedSliderY.toDouble(),
            sliderValue.toDouble(),
            fadeSpeedSliderHeight.toDouble(),
            Color(100, 150, 100).rgb
        )

        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "Speed: $ThemeFadeSpeed",
            fadeSpeedSliderX + 5,
            fadeSpeedSliderY - 15,
            Color.WHITE.rgb
        )

        // "Side" toggle button
        val toggleColor = if (updown) Color(0, 150, 0).rgb else Color(150, 0, 0).rgb
        DrRenderUtils.drawRect2(
            buttonX.toDouble(),
            buttonY.toDouble(),
            buttonWidth.toDouble(),
            buttonHeight.toDouble(),
            toggleColor
        )
        Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.drawString(
            "Side",
            buttonX + 2,
            buttonY + 2,
            Color.WHITE.rgb
        )
    }

    /**
     * Check if the mouse is hovering on any header button (UI, Configs, Color).
     */
    private fun isHoveringHeader(mouseX: Int, mouseY: Int): Boolean {
        val totalWidth = 3 * 60f + 2 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15

        var xOffset = 0f
        categories.forEach { _ ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(
                xVal - 30, yVal - 5,
                60f,
                (Fonts.SFBOLD.SFBOLD_26.SFBOLD_26.height + 10).toFloat(),
                mouseX, mouseY
            )
            if (hovered) {
                return true
            }
            xOffset += 60f + 10f
        }
        return false
    }

    private fun openFolder() {
        try {
            Desktop.getDesktop().open(fileManager.settingsDir)
            displayChatMessage("Opening configuration folder...")
        } catch (e: IOException) {
            displayChatMessage("Error opening folder: ${e.message}")
        }
    }

    /**
     * Overlays, gradients, bloom effect.
     */
    private fun drawOverlays(sr: ScaledResolution, alpha: Int, mouseX: Int, mouseY: Int) {
        // Vertical gradient
        DrRenderUtils.setAlphaLimit(0f)
        DrRenderUtils.drawGradientRect2(
            drag!!.x + 20.0,
            drag!!.y + 51.0,
            (rectWidth - 40).toDouble(),
            8.0,
            Color(0, 0, 0, (60 * (alpha / 255f)).toInt()).rgb,
            Color(0, 0, 0, 0).rgb
        )

        // Lateral gradient on the screen's right side
        val colorIndex = 0
        val moveAnimOut = moveOverGradientAnimation?.output?.toFloat() ?: 0f
        DrRenderUtils.drawGradientRectSideways2(
            (sr.scaledWidth - 40).toDouble(),
            0.0,
            40.0,
            sr.scaledHeight.toDouble(),
            DrRenderUtils.applyOpacity(generateColor(colorIndex).rgb, 0f),
            DrRenderUtils.applyOpacity(generateColor(colorIndex).rgb, 0.4f * moveAnimOut)
        )
        DrRenderUtils.setAlphaLimit(1f)

        // Bloom effect near mouse
        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))
    }
}
