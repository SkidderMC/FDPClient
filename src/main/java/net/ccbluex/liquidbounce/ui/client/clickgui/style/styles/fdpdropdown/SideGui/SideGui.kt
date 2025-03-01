/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.generateColor
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiBackgroundManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiBackgroundManager.bgHexFocused
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiBackgroundManager.bgHexInput
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiBackgroundManager.checkBackgroundInteractions
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiBackgroundManager.getBgHexFieldArea
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiColorManager.checkColorCategoryInteractions
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiColorManager.colorHexFocused
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiColorManager.colorHexInput
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiColorManager.getColorHexFieldArea
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers.SideGuiConfigsManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.ClientColorMode
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.interpolateFloat
import net.ccbluex.liquidbounce.utils.render.AnimationUtils.animate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import net.minecraft.client.gui.GuiScreen.getClipboardString
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class SideGui : GuiPanel() {

    private val categories = arrayOf("UI", "Configs", "Color", "Background")

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

    private var draggingSlider = false
    private var clickingHeader = false
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

        textAnimation = DecelerateAnimation(500, 1.0).apply { direction = Direction.BACKWARDS }
        clickAnimation = DecelerateAnimation(325, 1.0).apply { direction = Direction.BACKWARDS }
        hoverAnimation = DecelerateAnimation(250, 1.0).apply { direction = Direction.BACKWARDS }
        moveOverGradientAnimation = DecelerateAnimation(250, 1.0).apply { direction = Direction.BACKWARDS }

        categoryAnimation.clear()
        for (cat in categories) {
            categoryAnimation[cat] = arrayOf(
                DecelerateAnimation(250, 1.0),
                DecelerateAnimation(250, 1.0)
            )
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (colorHexFocused) {
            if (keyCode == Keyboard.KEY_RETURN) {
                ClientColorMode = colorHexInput
                colorHexFocused = false
                return
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (colorHexInput.length > 1) colorHexInput = colorHexInput.dropLast(1)
                return
            }
            val ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
            val shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
            if ((keyCode == Keyboard.KEY_V && ctrlDown) || (keyCode == Keyboard.KEY_INSERT && shiftDown)) {
                val clipboardStr = getClipboardString() ?: ""
                val filtered = clipboardStr.filter { it.toString().matches(Regex("[0-9A-Fa-f]")) }
                colorHexInput = "#${filtered.take(8)}"
                return
            }
            if (typedChar.toString().matches(Regex("[0-9A-Fa-f]"))) {
                if (colorHexInput.length < 9) colorHexInput += typedChar
            }
        }

        if (bgHexFocused) {
            if (keyCode == Keyboard.KEY_RETURN) {
                ClientThemesUtils.BackgroundMode = bgHexInput
                bgHexFocused = false
                return
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (bgHexInput.length > 1) bgHexInput = bgHexInput.dropLast(1)
                return
            }
            val ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
            val shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
            if ((keyCode == Keyboard.KEY_V && ctrlDown) || (keyCode == Keyboard.KEY_INSERT && shiftDown)) {
                val clipboardStr = getClipboardString() ?: ""
                val filtered = clipboardStr.filter { it.toString().matches(Regex("[0-9A-Fa-f]")) }
                bgHexInput = "#${filtered.take(8)}"
                return
            }
            if (typedChar.toString().matches(Regex("[0-9A-Fa-f]"))) {
                if (bgHexInput.length < 9) bgHexInput += typedChar
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float, alpha: Int) {
        assumeNonVolatile = true

        if (!draggingSlider && !clickingHeader) {
            handleMouseWheel()
        }
        animScroll = animate(animScroll, scroll, 0.5f)
        updateAnimations(mouseX, mouseY)

        val sr = ScaledResolution(MinecraftInstance.mc)
        val mainRectColor = drawMainPanel(sr, alpha, mouseX, mouseY)

        // Draw category tabs (centered)
        drawCategoryTabs(mouseX, mouseY, alpha)

        // Separator line
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
            "Configs" -> SideGuiConfigsManager.drawConfigsCategory(mouseX, mouseY, alpha, drag!!, rectWidth)
            "Color" -> SideGuiColorManager.drawColorCategory(mouseX, mouseY, alpha, drag!!, animScroll, rectHeight, smooth)
            "Background" -> SideGuiBackgroundManager.drawBackgroundCategory(mouseX, mouseY, alpha, drag!!, animScroll, rectHeight)
        }

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

        clickingHeader = isHoveringHeader(mouseX, mouseY)

        if (!draggingSlider && !clickingHeader) {
            val canDrag = DrRenderUtils.isHovering(drag!!.x, drag!!.y, rectWidth, 50f, mouseX, mouseY) ||
                    DrRenderUtils.isHovering(drag!!.x, drag!!.y, 20f, rectHeight, mouseX, mouseY)
            drag!!.onClick(mouseX, mouseY, button, canDrag)
        }

        checkCategoryClick(mouseX, mouseY)

        colorHexFocused = false
        bgHexFocused = false

        if (currentCategory == "Color") {
            checkColorCategoryInteractions(mouseX, mouseY, drag!!)
            val (hexX, hexY, hexW, hexH) = getColorHexFieldArea(drag!!)
            if (DrRenderUtils.isHovering(hexX, hexY, hexW, hexH, mouseX, mouseY)) {
                colorHexFocused = true
            }
        }
        if (currentCategory == "Background") {
            checkBackgroundInteractions(mouseX, mouseY)
            val (hexX, hexY, hexW, hexH) = getBgHexFieldArea(drag!!)
            if (DrRenderUtils.isHovering(hexX, hexY, hexW, hexH, mouseX, mouseY)) {
                bgHexFocused = true
            }
        }

        val fadeSpeedSliderX = drag!!.x + 25
        val fadeSpeedSliderY = drag!!.y + 20
        val fadeSpeedSliderWidth = 80f
        val fadeSpeedSliderHeight = 10f
        if (DrRenderUtils.isHovering(fadeSpeedSliderX, fadeSpeedSliderY, fadeSpeedSliderWidth, fadeSpeedSliderHeight, mouseX, mouseY)
            && button == 0) {
            draggingSlider = true
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        if (focused) {
            drag!!.onRelease(button)
            val sr = ScaledResolution(MinecraftInstance.mc)
            if (drag!!.x + rectWidth > sr.scaledWidth && clickAnimation!!.isDone) {
                focused = false
            }
        }
        draggingSlider = false
        clickingHeader = false
    }

    private fun drawUiCategory(alpha: Int) {
        Fonts.InterBold_26.drawString("Not Finished - Coming Soon", drag!!.x + rectWidth / 2, drag!!.y + rectHeight / 2, DrRenderUtils.applyOpacity(-1, alpha / 255f))
    }

    private fun handleMouseWheel() {
        val wheel = Mouse.getDWheel()
        if (wheel != 0) {
            scroll += if (wheel > 0) -30f else 30f
            scroll = max(-600f, min(0f, scroll))
        }
    }

    private fun updateAnimations(mouseX: Int, mouseY: Int) {
        clickAnimation?.direction =  if (focused) Direction.FORWARDS else Direction.BACKWARDS
        val hovering = DrRenderUtils.isHovering(drag!!.x, drag!!.y, rectWidth, rectHeight, mouseX, mouseY)
        hoverAnimation?.direction = if (hovering) Direction.FORWARDS else Direction.BACKWARDS
        val sr = ScaledResolution(MinecraftInstance.mc)
        val stillAnimating = !timerUtil!!.hasTimeElapsed(6000) || (!hoverAnimation!!.isDone || (hoverAnimation!!.isDone && hoverAnimation!!.direction == Direction.FORWARDS))
        textAnimation?.direction = if (!focused && stillAnimating) Direction.FORWARDS else Direction.BACKWARDS
        if (!clickAnimation!!.isDone) {
            drag!!.x = interpolateFloat(sr.scaledWidth - 30f, if (focused) sr.scaledWidth / 2f - rectWidth / 2f else drag!!.x, clickAnimation!!.output.toFloat().toDouble())
            drag!!.y = interpolateFloat(sr.scaledHeight / 2f - rectHeight / 2f, drag!!.y, clickAnimation!!.output.toFloat().toDouble())
        }
        val exceedingRightEdge = drag!!.x + rectWidth > sr.scaledWidth && (focused && clickAnimation!!.isDone && clickAnimation!!.direction == Direction.FORWARDS)
        moveOverGradientAnimation?.direction = if (exceedingRightEdge) Direction.FORWARDS else Direction.BACKWARDS
    }

    private fun drawMainPanel(sr: ScaledResolution, alpha: Int, mouseX: Int, mouseY: Int): Color {
        val hoverOut = hoverAnimation?.output ?: 0.0
        val clickOut = clickAnimation?.output ?: 0.0
        val moveOut = moveOverGradientAnimation?.output ?: 0.0

        var rectAlpha = min((185 + 30 * hoverOut + 70 * clickOut).toFloat() - (70 * moveOut).toFloat(), 255f)
        rectAlpha *= alpha / 255f

        val mainRectColor = Color(30, 30, 30, rectAlpha.toInt())
        if (focused) {
            if (!draggingSlider && !clickingHeader) {
                drag!!.onDraw(mouseX, mouseY)
            }
        }
        drawCustomShapeWithRadius(drag!!.x, drag!!.y, rectWidth, rectHeight, 9f, mainRectColor)
        if (showSideOutline) {
            val outlineColor = generateColor(0)
            drawRoundedOutline(drag!!.x, drag!!.y, drag!!.x + rectWidth, drag!!.y + rectHeight, 9f, 1f, outlineColor.rgb)
        }
        return mainRectColor
    }

    private fun drawCategoryTabs(mouseX: Int, mouseY: Int, alpha: Int) {
        val textColor = DrRenderUtils.applyOpacity(-1, alpha / 255f)
        val totalWidth = 4 * 60f + 3 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15

        var xOffset = 0f
        categories.forEachIndexed { index, cat ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(xVal - 30, yVal - 5, 60f, (Fonts.InterBold_26.height + 10).toFloat(), mouseX, mouseY)
            val catHoverAnim = categoryAnimation[cat]?.get(0)
            val catEnableAnim = categoryAnimation[cat]?.get(1)
            catHoverAnim?.direction = if (hovered) Direction.FORWARDS else Direction.BACKWARDS
            catEnableAnim?.direction = if (currentCategory == cat) Direction.FORWARDS else Direction.BACKWARDS
            val baseColor = Color(45, 45, 45, alpha)
            val colorToInterpolate = DrRenderUtils.applyOpacity(generateColor(index).rgb, alpha / 255f)
            val colorToInterpolateAsColor = Color(colorToInterpolate, true)
            val hoverOut = catHoverAnim?.output?.toFloat() ?: 0f
            val enableOut = catEnableAnim?.output?.toFloat() ?: 0f
            val hoverColor: Color = DrRenderUtils.interpolateColorC(baseColor, DrRenderUtils.brighter(baseColor, 0.8f), hoverOut)
            val finalColor: Color = DrRenderUtils.interpolateColorC(hoverColor, colorToInterpolateAsColor, enableOut)
            drawCustomShapeWithRadius(xVal - 30, yVal - 5, 60f, (Fonts.InterBold_26.height + 10).toFloat(), 6f, finalColor)
            Fonts.InterBold_26.drawCenteredString(cat, xVal, yVal, textColor)
            xOffset += 60f + 10f
        }
    }

    private fun checkCategoryClick(mouseX: Int, mouseY: Int) {
        val totalWidth = 4 * 60f + 3 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15
        var xOffset = 0f
        categories.forEach { cat ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(xVal - 30, yVal - 5, 60f, (Fonts.InterBold_26.height + 10).toFloat(), mouseX, mouseY)
            if (hovered) {
                currentCategory = cat
                return
            }
            xOffset += 60f + 10f
        }
    }

    /**
     * Check if the mouse is hovering on any header button (UI, Configs, Color).
     */
    private fun isHoveringHeader(mouseX: Int, mouseY: Int): Boolean {
        val totalWidth = 4 * 60f + 3 * 10f
        val startX = drag!!.x + rectWidth / 2f - totalWidth / 2f
        val yVal = drag!!.y + 15
        var xOffset = 0f
        categories.forEach { _ ->
            val xVal = startX + xOffset
            val hovered = DrRenderUtils.isHovering(xVal - 30, yVal - 5, 60f, (Fonts.InterBold_26.height + 10).toFloat(), mouseX, mouseY)
            if (hovered) return true
            xOffset += 60f + 10f
        }
        return false
    }

    /**
     * Overlays, gradients, bloom effect.
     */
    private fun drawOverlays(sr: ScaledResolution, alpha: Int, mouseX: Int, mouseY: Int) {
        DrRenderUtils.setAlphaLimit(0f)
        DrRenderUtils.drawGradientRect2(
            drag!!.x + 20.0,
            drag!!.y + 51.0,
            (rectWidth - 40).toDouble(),
            8.0,
            Color(0, 0, 0, (60 * (alpha / 255f)).toInt()).rgb,
            Color(0, 0, 0, 0).rgb
        )
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
        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))
    }
}