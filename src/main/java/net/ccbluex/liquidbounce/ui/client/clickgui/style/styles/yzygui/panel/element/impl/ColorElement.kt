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

    private var expanded = false
    private var showColorPicker = false
    private var colorPickerX = 0f
    private var colorPickerY = 0f
    private var hueSliderY = 0f
    private var opacitySliderY = 1f

    private val colorPickerWidth = 100
    private val colorPickerHeight = 80
    private val sliderWidth = 10
    private val sliderHeight = 80

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val currentColor = setting.get()

        // Draw color preview
        RenderUtils.yzyRectangle(
            (x + width - 15).toFloat(), y + 2f,
            12f, (height - 4).toFloat(),
            currentColor
        )

        // Draw setting name
        FDPClient.customFontManager["lato-bold-15"]?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        if (showColorPicker) {
            val pickerStartX = x + width + 5
            val pickerStartY = y

            // Draw color picker background
            RenderUtils.yzyRectangle(
                pickerStartX.toFloat(), pickerStartY.toFloat(),
                (colorPickerWidth + sliderWidth * 2 + 20).toFloat(),
                (colorPickerHeight + 30).toFloat(),
                Color(40, 40, 40, 200)
            )

            // Draw HSB color picker
            drawColorPicker(pickerStartX + 5, pickerStartY + 5, mouseX, mouseY)

            // Draw hue slider
            drawHueSlider(pickerStartX + colorPickerWidth + 10, pickerStartY + 5, mouseX, mouseY)

            // Draw opacity slider
            drawOpacitySlider(pickerStartX + colorPickerWidth + sliderWidth + 15, pickerStartY + 5, mouseX, mouseY, currentColor)

            // Draw RGB values
            val font = FDPClient.customFontManager["lato-bold-15"]
            font?.drawString("R: ${currentColor.red}", (pickerStartX + 5).toFloat(), (pickerStartY + colorPickerHeight + 10).toFloat(), Color.WHITE.rgb)
            font?.drawString("G: ${currentColor.green}", (pickerStartX + 35).toFloat(), (pickerStartY + colorPickerHeight + 10).toFloat(), Color.WHITE.rgb)
            font?.drawString("B: ${currentColor.blue}", (pickerStartX + 65).toFloat(), (pickerStartY + colorPickerHeight + 10).toFloat(), Color.WHITE.rgb)
            font?.drawString("A: ${currentColor.alpha}", (pickerStartX + 95).toFloat(), (pickerStartY + colorPickerHeight + 10).toFloat(), Color.WHITE.rgb)
        }

        // Draw RGB values if expanded (simple mode)
        if (expanded && !showColorPicker) {
            val font = FDPClient.customFontManager["lato-bold-15"]
            font?.drawString("R: ${currentColor.red}", (x + 1).toFloat(), y + height + 2f, Color(0xD2D2D2).rgb)
            font?.drawString("G: ${currentColor.green}", (x + 1).toFloat(), y + height + 14f, Color(0xD2D2D2).rgb)
            font?.drawString("B: ${currentColor.blue}", (x + 1).toFloat(), y + height + 26f, Color(0xD2D2D2).rgb)
        }
    }

    private fun drawColorPicker(startX: Int, startY: Int, mouseX: Int, mouseY: Int) {
        val hue = Color.RGBtoHSB(setting.get().red, setting.get().green, setting.get().blue, null)[0]

        // Draw color gradient
        for (px in 0 until colorPickerWidth) {
            for (py in 0 until colorPickerHeight) {
                val saturation = px / colorPickerWidth.toFloat()
                val brightness = 1.0f - (py / colorPickerHeight.toFloat())
                val color = Color.HSBtoRGB(hue, saturation, brightness)
                RenderUtils.yzyRectangle(
                    (startX + px).toFloat(), (startY + py).toFloat(),
                    1f, 1f, Color(color)
                )
            }
        }

        // Draw selection marker
        val markerX = startX + (colorPickerX * colorPickerWidth).roundToInt()
        val markerY = startY + (colorPickerY * colorPickerHeight).roundToInt()
        RenderUtils.yzyRectangle(
            markerX - 2f, markerY - 2f, 4f, 4f, Color.WHITE
        )
    }

    private fun drawHueSlider(startX: Int, startY: Int, mouseX: Int, mouseY: Int) {
        // Draw hue gradient
        for (y in 0 until sliderHeight) {
            val hue = y / sliderHeight.toFloat()
            val color = Color.HSBtoRGB(hue, 1.0f, 1.0f)
            RenderUtils.yzyRectangle(
                startX.toFloat(), (startY + y).toFloat(),
                sliderWidth.toFloat(), 1f, Color(color)
            )
        }

        // Draw hue marker
        val markerY = startY + (hueSliderY * sliderHeight).roundToInt()
        RenderUtils.yzyRectangle(
            (startX - 2).toFloat(), (markerY - 1).toFloat(),
            (sliderWidth + 4).toFloat(), 2f, Color.WHITE
        )
    }

    private fun drawOpacitySlider(startX: Int, startY: Int, mouseX: Int, mouseY: Int, currentColor: Color) {
        // Draw opacity gradient with checkerboard background
        for (y in 0 until sliderHeight) {
            val alpha = (1 - y / sliderHeight.toFloat()) * 255
            val baseColor = Color(currentColor.red, currentColor.green, currentColor.blue, alpha.roundToInt())

            // Checkerboard pattern
            val checkerSize = 4
            val isLight = ((y / checkerSize) + (0 / checkerSize)) % 2 == 0
            val bgColor = if (isLight) Color.WHITE else Color.LIGHT_GRAY

            val blendedColor = blendColors(bgColor, baseColor)
            RenderUtils.yzyRectangle(
                startX.toFloat(), (startY + y).toFloat(),
                sliderWidth.toFloat(), 1f, blendedColor
            )
        }

        // Draw opacity marker
        val markerY = startY + ((1 - opacitySliderY) * sliderHeight).roundToInt()
        RenderUtils.yzyRectangle(
            (startX - 2).toFloat(), (markerY - 1).toFloat(),
            (sliderWidth + 4).toFloat(), 2f, Color.WHITE
        )
    }

    private fun blendColors(bg: Color, fg: Color): Color {
        val alpha = fg.alpha / 255f
        val invAlpha = 1f - alpha
        return Color(
            (fg.red * alpha + bg.red * invAlpha).roundToInt(),
            (fg.green * alpha + bg.green * invAlpha).roundToInt(),
            (fg.blue * alpha + bg.blue * invAlpha).roundToInt()
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            when (button) {
                0 -> {
                    if (showColorPicker) {
                        handleColorPickerClick(mouseX, mouseY)
                    } else {
                        // Cycle through predefined colors
                        val colors = arrayOf(
                            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                            Color.CYAN, Color.MAGENTA, Color.WHITE, Color.ORANGE,
                            Color.PINK, Color.GRAY, Color.DARK_GRAY, Color.BLACK
                        )
                        val currentIndex = colors.indexOfFirst { it.rgb == setting.get().rgb }
                        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % colors.size else 0
                        setting.set(colors[nextIndex])
                    }
                }
                1 -> {
                    if (showColorPicker) {
                        showColorPicker = false
                    } else {
                        expanded = !expanded
                    }
                }
                2 -> showColorPicker = !showColorPicker
            }
        } else if (showColorPicker) {
            handleColorPickerClick(mouseX, mouseY)
        }
    }

    private fun handleColorPickerClick(mouseX: Int, mouseY: Int) {
        val pickerStartX = x + width + 5
        val pickerStartY = y

        // Color picker area
        val colorPickerArea = mouseX >= pickerStartX + 5 && mouseX <= pickerStartX + 5 + colorPickerWidth &&
                mouseY >= pickerStartY + 5 && mouseY <= pickerStartY + 5 + colorPickerHeight

        // Hue slider area
        val hueSliderArea = mouseX >= pickerStartX + colorPickerWidth + 10 &&
                mouseX <= pickerStartX + colorPickerWidth + 10 + sliderWidth &&
                mouseY >= pickerStartY + 5 && mouseY <= pickerStartY + 5 + sliderHeight

        // Opacity slider area
        val opacitySliderArea = mouseX >= pickerStartX + colorPickerWidth + sliderWidth + 15 &&
                mouseX <= pickerStartX + colorPickerWidth + sliderWidth + 15 + sliderWidth &&
                mouseY >= pickerStartY + 5 && mouseY <= pickerStartY + 5 + sliderHeight

        if (colorPickerArea) {
            colorPickerX = ((mouseX - pickerStartX - 5).toFloat() / colorPickerWidth).coerceIn(0f, 1f)
            colorPickerY = ((mouseY - pickerStartY - 5).toFloat() / colorPickerHeight).coerceIn(0f, 1f)
            updateColorFromPicker()
        } else if (hueSliderArea) {
            hueSliderY = ((mouseY - pickerStartY - 5).toFloat() / sliderHeight).coerceIn(0f, 1f)
            updateColorFromPicker()
        } else if (opacitySliderArea) {
            opacitySliderY = 1f - ((mouseY - pickerStartY - 5).toFloat() / sliderHeight).coerceIn(0f, 1f)
            updateColorFromPicker()
        }
    }

    private fun updateColorFromPicker() {
        val hue = hueSliderY
        val saturation = colorPickerX
        val brightness = 1f - colorPickerY
        val alpha = (opacitySliderY * 255).roundToInt()

        val rgb = Color.HSBtoRGB(hue, saturation, brightness)
        val color = Color(rgb)
        val finalColor = Color(color.red, color.green, color.blue, alpha)

        setting.set(finalColor)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}
}