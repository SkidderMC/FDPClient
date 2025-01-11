/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.scale
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui.clamp
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@SideOnly(Side.CLIENT)
object BlackStyle : Style() {
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        drawBorderedRect(
            panel.x, panel.y - 3, panel.x + panel.width, panel.y + 17, 3, Color(20, 20, 20).rgb, Color(20, 20, 20).rgb
        )

        if (panel.fade > 0) {
            drawBorderedRect(
                panel.x,
                panel.y + 17,
                panel.x + panel.width,
                panel.y + 19 + panel.fade,
                3,
                Color(40, 40, 40).rgb,
                Color(40, 40, 40).rgb
            )
            drawBorderedRect(
                panel.x,
                panel.y + 17 + panel.fade,
                panel.x + panel.width,
                panel.y + 24 + panel.fade,
                3,
                Color(20, 20, 20).rgb,
                Color(20, 20, 20).rgb
            )
        }

        val xPos = panel.x - (font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)) - 100) / 2
        font35.drawString(panel.name, xPos, panel.y + 4, Color.WHITE.rgb)
    }

    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val lines = text.lines()

        val width =
            lines.maxOfOrNull { font35.getStringWidth(it) + 14 } ?: return // Makes no sense to render empty lines
        val height = (font35.fontHeight * lines.size) + 3

        // Don't draw hover text beyond window boundaries
        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val x = mouseX.clamp(0, (scaledWidth / scale - width).roundToInt())
        val y = mouseY.clamp(0, (scaledHeight / scale - height).roundToInt())

        drawBorderedRect(x + 9, y, x + width, y + height, 3, Color(40, 40, 40).rgb, Color(40, 40, 40).rgb)

        lines.forEachIndexed { index, text ->
            font35.drawString(text, x + 12, y + 3 + (font35.fontHeight) * index, Color.WHITE.rgb)
        }
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        drawRect(
            buttonElement.x - 1,
            buttonElement.y - 1,
            buttonElement.x + buttonElement.width + 1,
            buttonElement.y + buttonElement.height + 1,
            getHoverColor(
                if (buttonElement.color != Int.MAX_VALUE) Color(20, 20, 20) else Color(40, 40, 40),
                buttonElement.hoverTime
            )
        )

        font35.drawString(buttonElement.displayName, buttonElement.x + 5, buttonElement.y + 5, Color.WHITE.rgb)
    }

    override fun drawModuleElementAndClick(
        mouseX: Int,
        mouseY: Int,
        moduleElement: ModuleElement,
        mouseButton: Int?
    ): Boolean {
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(Color(40, 40, 40), moduleElement.hoverTime)
        )
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(
                Color(20, 20, 20, moduleElement.slowlyFade),
                moduleElement.hoverTime,
                !moduleElement.module.isActive
            )
        )

        font35.drawString(
            moduleElement.displayName, moduleElement.x + 5, moduleElement.y + 5,
            if (moduleElement.module.state && !moduleElement.module.isActive) Color(255, 255, 255, 128).rgb
            else Color.WHITE.rgb
        )

        // Draw settings
        val moduleValues = moduleElement.module.values.filter { it.shouldRender() }
        if (moduleValues.isNotEmpty()) {
            font35.drawString(
                if (moduleElement.showSettings) "<" else ">",
                moduleElement.x + moduleElement.width - 8,
                moduleElement.y + 5,
                Color.WHITE.rgb
            )

            if (moduleElement.showSettings) {
                var yPos = moduleElement.y + 6

                val minX = moduleElement.x + moduleElement.width + 4
                val maxX = moduleElement.x + moduleElement.width + moduleElement.settingsWidth

                if (moduleElement.settingsWidth > 0 && moduleElement.settingsHeight > 0) drawBorderedRect(
                    minX,
                    yPos,
                    maxX,
                    yPos + moduleElement.settingsHeight,
                    3,
                    Color(20, 20, 20).rgb,
                    Color(40, 40, 40).rgb
                )

                for (value in moduleValues) {
                    assumeNonVolatile = value.get() is Number

                    val suffix = value.suffix ?: ""

                    when (value) {
                        is BoolValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                value.toggle()
                                clickSound()
                                return true
                            }

                            font35.drawString(
                                text, minX + 2, yPos + 2, if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE
                            )

                            yPos += 11
                        }

                        is ListValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 16

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + font35.fontHeight) {
                                value.openList = !value.openList
                                clickSound()
                                return true
                            }

                            font35.drawString(text, minX + 2, yPos + 2, Color.WHITE.rgb)
                            font35.drawString(
                                if (value.openList) "-" else "+",
                                (maxX - if (value.openList) 5 else 6),
                                yPos + 2,
                                Color.WHITE.rgb
                            )

                            yPos += font35.fontHeight + 1

                            for (valueOfList in value.values) {
                                moduleElement.settingsWidth = font35.getStringWidth("> $valueOfList") + 12

                                if (value.openList) {
                                    if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 9) {
                                        value.set(valueOfList)
                                        clickSound()
                                        return true
                                    }

                                    font35.drawString(
                                        "> $valueOfList",
                                        minX + 2,
                                        yPos + 2,
                                        if (value.get() == valueOfList) Color.WHITE.rgb else Int.MAX_VALUE
                                    )

                                    yPos += font35.fontHeight + 1
                                }
                            }
                            if (!value.openList) {
                                yPos += 1
                            }
                        }

                        is FloatValue -> {
                            val text = value.name + "§f: " + round(value.get()) + " §7$suffix"

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue = (x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)).roundToInt()

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(
                                    round(value.minimum + (value.maximum - value.minimum) * percentage).coerceIn(
                                        value.range
                                    )
                                )

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerValue -> {
                            val text = value.name + "§f: " + if (value is BlockValue) {
                                getBlockName(value.get()) + " (" + value.get() + ")"
                            } else {
                                value.get()
                            } + " §7$suffix"

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(value.lerpWith(percentage).coerceIn(value.range))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerRangeValue -> {
                            val slider1 = value.get().first
                            val slider2 = value.get().last

                            val text = "${value.name}§f: $slider1 - $slider2 §7$suffix§f (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            if (mouseButton == 0 && mouseX in x..x + width && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().first
                            val displayValue2 = value.get().last

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1, y + 1, 3f, color)
                            drawFilledCircle(sliderValue2, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FloatRangeValue -> {
                            val slider1 = value.get().start
                            val slider2 = value.get().endInclusive

                            val text = "${value.name}§f: ${round(slider1)} - ${round(slider2)} §7$suffix§f (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            if (mouseButton == 0 && mouseX in x..x + width && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum) / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum) / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().start
                            val displayValue2 = value.get().endInclusive

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y.toFloat(), sliderValue2, y + 2f, color.rgb)
                            drawFilledCircle(sliderValue1.roundToInt(), y + 1, 3f, color)
                            drawFilledCircle(sliderValue2.roundToInt(), y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FontValue -> {
                            val displayString = value.displayName
                            moduleElement.settingsWidth = font35.getStringWidth(displayString) + 8

                            font35.drawString(displayString, minX + 2, yPos + 2, Color.WHITE.rgb)

                            if (mouseButton != null && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                if (mouseButton == 0) value.next() else value.previous()
                                clickSound()
                                return true
                            }

                            yPos += 11
                        }

                        is ColorValue -> {
                            val currentColor = if (value.rainbow) {
                                value.get()
                            } else {
                                value.get()
                            }

                            val spacing = 10

                            val startX = moduleElement.x + moduleElement.width + 4
                            val startY = yPos - 1

                            // Color preview
                            val colorPreviewSize = 10
                            val colorPreviewX2 = maxX - colorPreviewSize
                            val colorPreviewX1 = colorPreviewX2 - colorPreviewSize
                            val colorPreviewY1 = startY + 1
                            val colorPreviewY2 = colorPreviewY1 + colorPreviewSize

                            // Text
                            val textX = startX + 2F
                            val textY = startY + 3F

                            // Sliders
                            val hueSliderWidth = 7
                            val hueSliderHeight = 50
                            val colorPickerWidth = 75
                            val colorPickerHeight = 50

                            val colorPickerStartX = textX.toInt()
                            val colorPickerEndX = colorPickerStartX + colorPickerWidth
                            val colorPickerStartY = colorPreviewY2 + spacing / 3
                            val colorPickerEndY = colorPickerStartY + colorPickerHeight

                            val hueSliderStartY = colorPickerStartY
                            val hueSliderEndY = colorPickerStartY + hueSliderHeight

                            if (mouseButton == 0 && mouseX in colorPreviewX1..colorPreviewX2 && mouseY in colorPreviewY1..colorPreviewY2) {
                                value.showPicker = !value.showPicker
                                clickSound()
                                return true
                            }

                            val display = "Color: ${"#%08X".format(currentColor.rgb)}"

                            moduleElement.settingsWidth = font35.getStringWidth(display) + 8

                            font35.drawString(display, textX, textY, Color.WHITE.rgb)

                            drawRect(colorPreviewX1, colorPreviewY1, colorPreviewX2, colorPreviewY2, currentColor.rgb)

                            // Rainbow rectangle next to it?
                            // Maybe with a blue border to indicate which one is chosen?

                            if (value.showPicker) {
                                if (!value.rainbow) {
                                    val hueSliderColor = value.hueSliderColor

                                    var (hue, saturation, brightness) = Color.RGBtoHSB(
                                        hueSliderColor.red, hueSliderColor.green, hueSliderColor.blue, null
                                    )
                                    var (currHue, currSaturation, currBrightness) = Color.RGBtoHSB(
                                        currentColor.red, currentColor.green, currentColor.blue, null
                                    )

                                    // Color Picker
                                    value.updateTextureCache(
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
                                            drawTexture(
                                                id,
                                                colorPickerStartX,
                                                colorPickerStartY,
                                                colorPickerWidth,
                                                colorPickerHeight
                                            )
                                        })

                                    val markerX = colorPickerStartX + (currSaturation * colorPickerWidth).toInt()
                                    val markerY = colorPickerStartY + ((1.0f - currBrightness) * colorPickerHeight).toInt()

                                    RenderUtils.drawBorder(
                                        markerX - 2f, markerY - 2f, markerX + 3f, markerY + 3f, 1.5f, Color.WHITE.rgb
                                    )

                                    val hueSliderX = colorPickerEndX + 5

                                    // Hue slider
                                    value.updateTextureCache(
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
                                            drawTexture(
                                                id,
                                                hueSliderX,
                                                colorPickerStartY,
                                                hueSliderWidth,
                                                hueSliderHeight
                                            )
                                        })

                                    val hueMarkerY = hueSliderStartY + (hue * hueSliderHeight)

                                    RenderUtils.drawBorder(
                                        hueSliderX.toFloat() - 1,
                                        hueMarkerY - 1f,
                                        hueSliderX + hueSliderWidth + 1f,
                                        (hueMarkerY + 1).coerceAtMost((hueSliderEndY).toFloat()) + 1,
                                        1.5f,
                                        Color.WHITE.rgb,
                                    )

                                    val inColorPicker =
                                        mouseX in colorPickerStartX until colorPickerEndX && mouseY in colorPickerStartY until colorPickerEndY
                                    val inHueSlider =
                                        mouseX in hueSliderX - 1..hueSliderX + hueSliderWidth + 1 && mouseY in hueSliderStartY until hueSliderEndY

                                    if ((mouseButton == 0 || sliderValueHeld == value) && (inColorPicker || inHueSlider)) {
                                        if (inColorPicker) {
                                            val newS = ((mouseX - colorPickerStartX) / colorPickerWidth.toFloat()).coerceIn(0f, 1f)
                                            val newB =
                                                (1.0f - (mouseY - colorPickerStartY) / colorPickerHeight.toFloat()).coerceIn(
                                                    0f,
                                                    1f
                                                )
                                            saturation = newS
                                            brightness = newB
                                        }

                                        var finalColor = Color(Color.HSBtoRGB(hue, saturation, brightness))

                                        if (inHueSlider) {
                                            currHue = ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(
                                                0f,
                                                1f
                                            )

                                            finalColor = Color(Color.HSBtoRGB(currHue, currSaturation, currBrightness))
                                        }

                                        sliderValueHeld = value

                                        if (inHueSlider) {
                                            value.hueSliderColor = finalColor
                                        }

                                        value.setAndSaveValueOnButtonRelease(finalColor)

                                        if (mouseButton == 0) {
                                            return true
                                        }
                                    }
                                    yPos += colorPickerHeight - colorPreviewSize + spacing + 6
                                }
                            }
                            yPos += spacing
                        }

                        else -> {
                            val text = value.name + "§f: " + value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 12
                        }
                    }
                }

                moduleElement.settingsHeight = yPos - moduleElement.y - 6

                if (mouseButton != null && mouseX in minX..maxX && mouseY in moduleElement.y + 6..yPos + 2) return true
            }
        }

        if (mouseButton == -1) {
            sliderValueHeld = null
        }

        return false
    }
}