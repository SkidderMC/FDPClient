/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexture
import net.ccbluex.liquidbounce.utils.render.RenderUtils.updateTextureCache
import net.ccbluex.liquidbounce.utils.ui.EditableText
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle.chosenText
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Color Element - YZY GUI
 * @author opZywl
 */
class ColorElement(
    private val element: ModuleElement,
    private val setting: ColorValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private val colorPickerWidth = 75
    private val colorPickerHeight = 50
    private val hueSliderWidth = 7
    private val hueSliderHeight = 50
    private val spacingBetweenSliders = 5

    fun getActualHeight(): Int {
        var totalHeight = 30 // Base height for name and color code

        if (setting.showOptions) {
            totalHeight += 12 * 4 + 8 // RGBA options height
        }

        if (setting.showPicker) {
            totalHeight += colorPickerHeight + 10 // Color picker height + spacing
        }

        return totalHeight
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.height = getActualHeight()

        val currentColor = setting.selectedColor()
        val textColor = Color.WHITE

        val font = FDPClient.customFontManager["lato-bold-15"]

        // Draw setting name and color code
        val labelText = setting.name + ":"
        font?.drawString(
            labelText,
            (x + 5).toFloat(),
            (y + 3).toFloat(),
            textColor.rgb
        )

        val colorCodeText = "#%08X".format(currentColor.rgb)
        font?.drawString(
            colorCodeText,
            (x + 5).toFloat(),
            (y + 3 + 12 + 2).toFloat(),
            textColor.rgb
        )

        val previewSize = 9
        val previewX2 = x + width - 10
        val previewX1 = previewX2 - previewSize
        val previewY1 = y + 2
        val previewY2 = previewY1 + previewSize

        // Main color preview
        RenderUtils.drawRect(previewX1.toFloat(), previewY1.toFloat(), previewX2.toFloat(), previewY2.toFloat(), currentColor.rgb)

        // Rainbow preview
        val rainbowPreviewX2 = previewX1 - previewSize
        val rainbowPreviewX1 = rainbowPreviewX2 - previewSize
        if (rainbowPreviewX1 > x + 4) {
            RenderUtils.drawRect(
                rainbowPreviewX1.toFloat(), previewY1.toFloat(),
                rainbowPreviewX2.toFloat(), previewY2.toFloat(),
                ColorUtils.rainbow(setting.opacitySliderY).rgb
            )
        }

        val extraOptionsHeight = if (setting.showOptions && !setting.showPicker) {
            val rgbaLabels = listOf("R", "G", "B", "A")
            val rgbaValues = listOf(currentColor.red, currentColor.green, currentColor.blue, currentColor.alpha)
            val labelWidth = rgbaLabels.maxOfOrNull {
                font?.getWidth(it) ?: 10
            }?.toFloat() ?: 10f
            val optionStartY = y + 3 + 12 * 2 + 8 // Added more spacing
            var optionY = optionStartY

            rgbaLabels.forEachIndexed { index, label ->
                val valueText = rgbaValues[index].toString()
                font?.drawString("$label:", (x + 5).toFloat(), optionY.toFloat(), textColor.rgb)

                val valueTextColor = if (chosenText != null && chosenText!!.value == setting && setting.rgbaIndex == index) {
                    Color.WHITE
                } else {
                    Color.LIGHT_GRAY
                }

                font?.drawString(
                    valueText,
                    (x + 5).toFloat() + labelWidth + 10f,
                    optionY.toFloat(),
                    valueTextColor.rgb
                )
                optionY += 12 + 2 // Reduced spacing between RGBA options
            }
            (optionY - optionStartY).toFloat()
        } else 0f

        if (setting.showPicker) {
            // Draw semi-transparent background to prevent overlap
            val pickerBackgroundY = y + 30 + extraOptionsHeight.toInt()
            val pickerBackgroundHeight = colorPickerHeight + 20
            RenderUtils.drawRect(
                (x + 2).toFloat(),
                pickerBackgroundY.toFloat(),
                (x + width - 2).toFloat(),
                (pickerBackgroundY + pickerBackgroundHeight).toFloat(),
                Color(0, 0, 0, 180).rgb
            )

            val colorPickerStartX = x + 5
            val colorPickerStartY = pickerBackgroundY + 5
            val colorPickerEndX = colorPickerStartX + colorPickerWidth
            val colorPickerEndY = colorPickerStartY + colorPickerHeight
            val hueSliderX = colorPickerEndX + spacingBetweenSliders
            val hueSliderEndY = colorPickerStartY + hueSliderHeight
            val opacityStartX = hueSliderX + hueSliderWidth + spacingBetweenSliders
            val opacityEndX = opacityStartX + hueSliderWidth

            val hue = if (setting.rainbow) {
                Color.RGBtoHSB(currentColor.red, currentColor.green, currentColor.blue, null)[0]
            } else {
                setting.hueSliderY
            }

            // Color picker texture
            try {
                setting.updateTextureCache(
                    id = 0,
                    hue = hue,
                    width = colorPickerWidth,
                    height = colorPickerHeight,
                    generateImage = { image, _ ->
                        for (px in 0 until colorPickerWidth) {
                            for (py in 0 until colorPickerHeight) {
                                val localS = px / colorPickerWidth.toFloat()
                                val localB = 1.0f - (py / colorPickerHeight.toFloat())
                                val rgb = Color.HSBtoRGB(hue, localS, localB)
                                image.setRGB(px, py, rgb)
                            }
                        }
                    },
                    drawAt = { id ->
                        drawTexture(id, colorPickerStartX, colorPickerStartY, colorPickerWidth, colorPickerHeight)
                    }
                )
            } catch (e: Exception) {
                // Fallback: draw solid color rectangle if texture fails
                RenderUtils.drawRect(
                    colorPickerStartX.toFloat(), colorPickerStartY.toFloat(),
                    colorPickerEndX.toFloat(), colorPickerEndY.toFloat(),
                    Color.HSBtoRGB(hue, 1.0f, 1.0f)
                )
            }

            // Color picker marker
            val markerX = (colorPickerStartX..colorPickerEndX).let { range ->
                range.first + (range.last - range.first) * setting.colorPickerPos.x
            }
            val markerY = (colorPickerStartY..colorPickerEndY).let { range ->
                range.first + (range.last - range.first) * setting.colorPickerPos.y
            }

            if (!setting.rainbow) {
                RenderUtils.drawBorder(markerX - 2f, markerY - 2f, markerX + 3f, markerY + 3f, 1.5f, Color.WHITE.rgb)
            }

            // Hue slider texture
            try {
                setting.updateTextureCache(
                    id = 1,
                    hue = hue,
                    width = hueSliderWidth,
                    height = hueSliderHeight,
                    generateImage = { image, _ ->
                        for (y in 0 until hueSliderHeight) {
                            for (x in 0 until hueSliderWidth) {
                                val localHue = y / hueSliderHeight.toFloat()
                                val rgb = Color.HSBtoRGB(localHue, 1.0f, 1.0f)
                                image.setRGB(x, y, rgb)
                            }
                        }
                    },
                    drawAt = { id ->
                        drawTexture(id, hueSliderX, colorPickerStartY, hueSliderWidth, hueSliderHeight)
                    }
                )
            } catch (e: Exception) {
                // Fallback: draw gradient manually
                for (y in 0 until hueSliderHeight) {
                    val localHue = y / hueSliderHeight.toFloat()
                    val rgb = Color.HSBtoRGB(localHue, 1.0f, 1.0f)
                    RenderUtils.drawRect(
                        hueSliderX.toFloat(), (colorPickerStartY + y).toFloat(),
                        (hueSliderX + hueSliderWidth).toFloat(), (colorPickerStartY + y + 1).toFloat(),
                        rgb
                    )
                }
            }

            // Opacity slider texture
            try {
                setting.updateTextureCache(
                    id = 2,
                    hue = currentColor.rgb.toFloat(),
                    width = hueSliderWidth,
                    height = hueSliderHeight,
                    generateImage = { image, _ ->
                        val gridSize = 1
                        for (y in 0 until hueSliderHeight) {
                            for (x in 0 until hueSliderWidth) {
                                val gridX = x / gridSize
                                val gridY = y / gridSize
                                val checkerboardColor = if ((gridY + gridX) % 2 == 0) {
                                    Color.WHITE.rgb
                                } else {
                                    Color.BLACK.rgb
                                }
                                val alpha = ((1 - y.toFloat() / hueSliderHeight.toFloat()) * 255).roundToInt()
                                val finalColor = ColorUtils.blendColors(Color(checkerboardColor), Color(currentColor.red, currentColor.green, currentColor.blue, alpha))
                                image.setRGB(x, y, finalColor.rgb)
                            }
                        }
                    },
                    drawAt = { id ->
                        drawTexture(id, opacityStartX, colorPickerStartY, hueSliderWidth, hueSliderHeight)
                    }
                )
            } catch (e: Exception) {
                // Fallback: draw opacity gradient manually
                for (y in 0 until hueSliderHeight) {
                    val alpha = ((1 - y.toFloat() / hueSliderHeight.toFloat()) * 255).roundToInt()
                    val color = Color(currentColor.red, currentColor.green, currentColor.blue, alpha)
                    RenderUtils.drawRect(
                        opacityStartX.toFloat(), (colorPickerStartY + y).toFloat(),
                        opacityEndX.toFloat(), (colorPickerStartY + y + 1).toFloat(),
                        color.rgb
                    )
                }
            }

            // Draw slider markers
            val hueMarkerY = (colorPickerStartY..hueSliderEndY).let { range ->
                range.first + (range.last - range.first) * hue
            }
            val opacityMarkerY = (colorPickerStartY..hueSliderEndY).let { range ->
                range.first + (range.last - range.first) * (1 - setting.opacitySliderY)
            }

            RenderUtils.drawBorder(
                hueSliderX.toFloat() - 1, hueMarkerY - 1f,
                hueSliderX + hueSliderWidth + 1f, hueMarkerY + 1f,
                1.5f, Color.WHITE.rgb
            )

            RenderUtils.drawBorder(
                opacityStartX.toFloat() - 1, opacityMarkerY - 1f,
                opacityEndX + 1f, opacityMarkerY + 1f,
                1.5f, Color.WHITE.rgb
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val currentColor = setting.selectedColor()
        val previewSize = 9
        val previewX2 = x + width - 10
        val previewX1 = previewX2 - previewSize
        val previewY1 = y + 2
        val previewY2 = previewY1 + previewSize
        val rainbowPreviewX2 = previewX1 - previewSize
        val rainbowPreviewX1 = rainbowPreviewX2 - previewSize

        val isColorPreview = mouseX in previewX1..previewX2 && mouseY in previewY1..previewY2
        val isRainbowPreview = mouseX in rainbowPreviewX1..rainbowPreviewX2 && mouseY in previewY1..previewY2

        if (button in arrayOf(0, 1)) {
            when {
                isColorPreview -> {
                    if (button == 0 && setting.rainbow) setting.rainbow = false
                    if (button == 1) setting.showPicker = !setting.showPicker
                    return
                }
                isRainbowPreview -> {
                    if (button == 0) setting.rainbow = true
                    if (button == 1) setting.showPicker = !setting.showPicker
                    return
                }
            }
        }

        val font = FDPClient.customFontManager["lato-bold-15"]
        val colorCodeText = "#%08X".format(currentColor.rgb)
        val hexTextWidth = font?.getWidth(colorCodeText)?.toFloat() ?: 50f
        val hexTextX = x + 5f
        val hexTextY = y + 3f + 12 + 2

        if (button == 1 && mouseX >= hexTextX && mouseX <= hexTextX + hexTextWidth &&
            mouseY >= hexTextY && mouseY <= hexTextY + 12) {
            setting.showOptions = !setting.showOptions
            return
        }

        if (setting.showOptions && button == 0) {
            val rgbaLabels = listOf("R", "G", "B", "A")
            val rgbaValues = listOf(currentColor.red, currentColor.green, currentColor.blue, currentColor.alpha)
            val labelWidth = rgbaLabels.maxOfOrNull {
                font?.getWidth(it) ?: 10
            }?.toFloat() ?: 10f
            val optionStartY = y + 3 + 12 * 2 + 8 // Added more spacing

            rgbaLabels.forEachIndexed { index, _ ->
                val yPosition = optionStartY + index * (12 + 2)
                val rgbaTextX = (x + 5).toFloat() + labelWidth + 10f
                val rgbaTextY = yPosition - 2
                val rgbaTextHeight = 12 + 2
                val maxWidth = font?.getWidth("255")?.toFloat() ?: 30f

                if (mouseX >= rgbaTextX && mouseX <= rgbaTextX + maxWidth &&
                    mouseY >= rgbaTextY && mouseY <= rgbaTextY + rgbaTextHeight) {
                    chosenText = EditableText.forRGBA(setting, index)
                    setting.rgbaIndex = index
                    return
                }
            }
        }

        if (setting.showPicker) {
            val extraOptionsHeight = if (setting.showOptions) 12 * 4 + 8 else 0f
            val colorPickerStartX = x + 5
            val colorPickerStartY = y + 30 + extraOptionsHeight.toInt() + 5
            val colorPickerEndX = colorPickerStartX + colorPickerWidth
            val colorPickerEndY = colorPickerStartY + colorPickerHeight
            val hueSliderX = colorPickerEndX + spacingBetweenSliders
            val hueSliderEndY = colorPickerStartY + hueSliderHeight
            val opacityStartX = hueSliderX + hueSliderWidth + spacingBetweenSliders
            val opacityEndX = opacityStartX + hueSliderWidth

            val inColorPicker = mouseX in colorPickerStartX until colorPickerEndX &&
                    mouseY in colorPickerStartY until colorPickerEndY && !setting.rainbow
            val inHueSlider = mouseX in hueSliderX - 1..hueSliderX + hueSliderWidth + 1 &&
                    mouseY in colorPickerStartY until hueSliderEndY && !setting.rainbow
            val inOpacitySlider = mouseX in opacityStartX - 1..opacityEndX + 1 &&
                    mouseY in colorPickerStartY until hueSliderEndY

            if (button == 0 && (inColorPicker || inHueSlider || inOpacitySlider)) {
                if (inColorPicker) {
                    val newS = ((mouseX - colorPickerStartX) / colorPickerWidth.toFloat()).coerceIn(0f, 1f)
                    val newB = (1.0f - (mouseY - colorPickerStartY) / colorPickerHeight.toFloat()).coerceIn(0f, 1f)
                    setting.colorPickerPos.x = newS
                    setting.colorPickerPos.y = 1 - newB
                }

                if (inHueSlider) {
                    setting.hueSliderY = ((mouseY - colorPickerStartY) / hueSliderHeight.toFloat()).coerceIn(0f, 1f)
                }

                if (inOpacitySlider) {
                    setting.opacitySliderY = 1 - ((mouseY - colorPickerStartY) / hueSliderHeight.toFloat()).coerceIn(0f, 1f)
                }

                // Update color based on picker values
                try {
                    var finalColor = Color(Color.HSBtoRGB(setting.hueSliderY, setting.colorPickerPos.x, 1 - setting.colorPickerPos.y))
                    finalColor = Color(finalColor.red, finalColor.green, finalColor.blue, (setting.opacitySliderY * 255).roundToInt())
                    setting.set(finalColor)
                } catch (e: Exception) {
                    // Fallback to current color if update fails
                    println("Failed to update color: ${e.message}")
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}
}