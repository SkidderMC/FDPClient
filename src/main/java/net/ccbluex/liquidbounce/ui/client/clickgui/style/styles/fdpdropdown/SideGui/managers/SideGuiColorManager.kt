/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers

import net.ccbluex.liquidbounce.FDPClient.fileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.ClientColorMode
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.ThemeFadeSpeed
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColorFromName
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.updown
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.render.AnimationUtils.animate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class Quad(val x: Float, val y: Float, val w: Float, val h: Float)

object SideGuiColorManager {

    var colorHexInput = "#"
    var colorHexFocused = false

    fun drawColorCategory(
        mouseX: Int,
        mouseY: Int,
        alpha: Int,
        drag: Drag,
        animScroll: Float,
        rectHeight: Float,
        smooth: FloatArray
    ) {
        val themeColors = arrayOf(
            "FDP", "Zywl", "Water", "Magic", "DarkNight", "Sun",
            "Tree", "Flower", "Loyoi", "Cero",
            "May", "Mint", "Azure", "Rainbow", "Astolfo",
            "Pumpkin", "Polarized", "Sundae", "Terminal", "Coral",
            "Fire", "Aqua", "Peony", "Magics", "EveningSunshine", "LightOrange",
            "Reef", "Amin", "MangoPulp", "MoonPurple", "Aqualicious",
            "Stripe", "Shifter", "Quepal", "Orca", "SublimeVivid",
            "MoonAsteroid", "SummerDog", "PinkFlavour", "SinCityRed",
            "Timber", "PinotNoir", "DirtyFog", "Piglet", "LittleLeaf",
            "Nelson", "TurquoiseFlow", "Purplin", "Martini", "SoundCloud",
            "Inbox", "Amethyst", "Blush", "MochaRose"
        )

        val colorXStart = drag.x + 25
        val colorYStart = drag.y + 60 + animScroll
        val colorWidth = 80f
        val colorHeight = 60f
        val colorsPerRow = 5
        var colorX = colorXStart
        var colorY = colorYStart
        val maxVisibleHeight = drag.y + rectHeight - 60

        themeColors.forEachIndexed { i, colorName ->
            if (colorY + colorHeight > drag.y + 60 && colorY < maxVisibleHeight) {
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
                    startColor,
                    endColor,
                    0f
                )

                val isSelected = (ClientColorMode == colorName)
                if (isSelected) {
                    smooth[0] = animate(smooth[0], colorX, 0.02f * deltaTime)
                    smooth[1] = animate(smooth[1], colorY, 0.02f * deltaTime)
                    smooth[2] = animate(smooth[2], colorX + colorWidth, 0.02f * deltaTime)
                    smooth[3] = animate(smooth[3], colorY + colorHeight, 0.02f * deltaTime)
                    drawRoundedOutline(smooth[0], smooth[1], smooth[2], smooth[3], 10f, 3f, Color(startColor).brighter().rgb)
                }
                Fonts.InterBold_26.drawCenteredStringShadow(
                    colorName,
                    colorX + colorWidth / 2f,
                    colorY + colorHeight / 2f - Fonts.InterBold_26.height / 2,
                    Color.WHITE.rgb
                )
            }
            colorX += colorWidth + 10
            if ((i + 1) % colorsPerRow == 0) {
                colorX = colorXStart
                colorY += colorHeight + 10
            }
        }
        drawColorExtras(mouseX, mouseY, alpha, colorXStart, drag.y + 60, colorWidth, drag)

        val totalRows = ceil(themeColors.size / colorsPerRow.toDouble()).toInt()
        val totalContentHeight = totalRows * (colorHeight.toInt() + 10) - 10
        val visibleHeight = (drag.y + rectHeight - 60) - (drag.y + 60)
        if (totalContentHeight > visibleHeight) {
            val thumbHeight = visibleHeight * visibleHeight / totalContentHeight
            val scrollRange = totalContentHeight - visibleHeight
            val currentScrollVal = -animScroll
            val thumbY = if (scrollRange > 0) (currentScrollVal / scrollRange) * (visibleHeight - thumbHeight) else 0f
            val buttonWidth = 50f
            val scrollbarX = drag.x + 25 + (colorWidth + 10) * colorsPerRow + buttonWidth + 5
            val scrollbarY = drag.y + 60
            DrRenderUtils.drawRect2(
                scrollbarX.toDouble(),
                scrollbarY.toDouble(),
                5.0,
                visibleHeight.toDouble(),
                Color(50, 50, 50, alpha).rgb
            )
            DrRenderUtils.drawRect2(
                scrollbarX.toDouble(),
                (scrollbarY + thumbY).toDouble(),
                5.0,
                thumbHeight.toDouble(),
                Color(150, 150, 150, alpha).rgb
            )
        }
    }

    fun getColorHexFieldArea(drag: Drag): Quad {
        val sideBtnX = drag.x + 25 + (80f + 10) * 5
        val sideBtnY = drag.y + 60
        val sideBtnW = 50f
        val sideBtnH = 15f

        val textW = 80f
        val textH = 14f

        val x = sideBtnX + (sideBtnW - textW) / 2f
        val y = sideBtnY - textH - 20
        return Quad(x, y, textW, textH)
    }

    fun drawColorExtras(
        mouseX: Int,
        mouseY: Int,
        alpha: Int,
        colorXStart: Float,
        buttonBaseY: Float,
        colorWidth: Float,
        drag: Drag
    ) {
        val buttonX = colorXStart + (colorWidth + 10) * 5
        val buttonY = buttonBaseY
        val buttonWidth = 50f
        val buttonHeight = 15f
        val fadeSpeedSliderX = drag.x + 25
        val fadeSpeedSliderY = drag.y + 20
        val fadeSpeedSliderWidth = 80f
        val fadeSpeedSliderHeight = 10f

        // Fade speed slider
        DrRenderUtils.drawRect2(
            fadeSpeedSliderX.toDouble(),
            fadeSpeedSliderY.toDouble(),
            fadeSpeedSliderWidth.toDouble(),
            fadeSpeedSliderHeight.toDouble(),
            Color(60, 60, 60).rgb
        )
        val sliderValue = (ThemeFadeSpeed / 10f) * fadeSpeedSliderWidth
        DrRenderUtils.drawRect2(
            fadeSpeedSliderX.toDouble(),
            fadeSpeedSliderY.toDouble(),
            sliderValue.toDouble(),
            fadeSpeedSliderHeight.toDouble(),
            Color(100, 150, 100).rgb
        )
        Fonts.InterBold_26.drawString("Speed: $ThemeFadeSpeed", fadeSpeedSliderX + 5, fadeSpeedSliderY - 15, Color.WHITE.rgb)

        val toggleColor = if (updown) Color(0, 150, 0).rgb else Color(150, 0, 0).rgb
        DrRenderUtils.drawRect2(buttonX.toDouble(), buttonY.toDouble(), buttonWidth.toDouble(), buttonHeight.toDouble(), toggleColor)
        Fonts.InterBold_26.drawString("Side", buttonX + 2, buttonY + 2, Color.WHITE.rgb)

        val hexField = getColorHexFieldArea(drag)
        DrRenderUtils.drawRect2(
            hexField.x.toDouble(),
            hexField.y.toDouble(),
            hexField.w.toDouble(),
            hexField.h.toDouble(),
            Color(40, 40, 40, alpha).rgb
        )

        Fonts.InterBold_26.drawString("Hex:", hexField.x, hexField.y - 12, DrRenderUtils.applyOpacity(-1, alpha / 255f))
    }

    fun checkColorCategoryInteractions(mouseX: Int, mouseY: Int, drag: Drag) {
        val buttonX = drag.x + 25 + (80f + 10) * 5
        val buttonY = drag.y + 60
        val buttonW = 50f
        val buttonH = 15f
        val hoveredToggle = DrRenderUtils.isHovering(buttonX, buttonY, buttonW, buttonH, mouseX, mouseY)
        if (hoveredToggle) {
            updown = !updown
        }
        // Fade speed slider
        val sliderX = drag.x + 25
        val sliderY = drag.y + 20
        val sliderW = 80f
        val sliderH = 10f
        val hoveredSlider = DrRenderUtils.isHovering(sliderX, sliderY, sliderW, sliderH, mouseX, mouseY)
        if (hoveredSlider) {
            var newSpeed = ((mouseX - sliderX) / sliderW) * 10
            newSpeed = max(0f, min(10f, newSpeed))
            ThemeFadeSpeed = newSpeed.toInt()
        }
    }
}